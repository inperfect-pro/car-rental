package pro.inperfect.car_rental.component;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient.ResponseSpec;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pro.inperfect.car_rental.base.AbstractWholeContextTest;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ReservationComponentTest extends AbstractWholeContextTest {

    private static final String RESERVATIONS_PATH = "/api/reservations";
    private static final String RENTAL_START = "2030-01-10T10:15:30Z";
    private static final String RENTAL_END_AFTER_TWO_DAYS = "2030-01-12T10:15:30Z";
    private static final int RENTAL_DAYS = 2;
    private static final int FLEET_CAPACITY = 3;
    private static final String UNSUPPORTED_CAR_TYPE = "CONVERTIBLE";
    private static final Duration LOCK_WAIT_TIMEOUT = Duration.ofSeconds(4);
    private static final long RESPONSE_TIMEOUT_SECONDS = 10;

    private final RestTestClient httpClient;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactions;

    @Autowired
    ReservationComponentTest(
        RestTestClient httpClient, JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.httpClient = httpClient;
        this.jdbcTemplate = jdbcTemplate;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    void deleteReservations() {
        jdbcTemplate.update("delete from car_reservation");
    }

    @ParameterizedTest
    @EnumSource(CarType.class)
    void given_supported_car_type_when_reservation_is_requested_should_create_reservation(CarType carType) {
        // given
        String requestBody = reservationRequest(carType, RENTAL_DAYS);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        EntityExchangeResult<byte[]> created = response.expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.carType").isEqualTo(carType.name())
            .jsonPath("$.startsAt").isEqualTo(RENTAL_START)
            .jsonPath("$.endsAt").isEqualTo(RENTAL_END_AFTER_TWO_DAYS)
            .returnResult();

        String body = new String(created.getResponseBodyContent(), StandardCharsets.UTF_8);
        String reservationId = JsonPath.read(body, "$.reservationId");
        assertThat(reservationId).as("reservation ID").isNotBlank();
        assertThat(created.getResponseHeaders().getLocation())
            .isEqualTo(URI.create(RESERVATIONS_PATH + "/" + reservationId));
    }

    @Test
    void given_car_type_at_capacity_when_overlapping_reservation_is_requested_should_return_conflict() {
        // given
        String requestBody = reservationRequest(CarType.SUV, RENTAL_DAYS);
        createReservations(requestBody, FLEET_CAPACITY);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
            .jsonPath("$.title").isEqualTo("Car type unavailable")
            .jsonPath("$.instance").isEqualTo(RESERVATIONS_PATH)
            .jsonPath("$.carType").isEqualTo(CarType.SUV.name());
    }

    @Test
    void given_staggered_bookings_when_a_rental_spans_them_should_create_the_reservation() {
        // given
        Instant firstPickup = Instant.parse(RENTAL_START);
        for (int day = 0; day < FLEET_CAPACITY; day++) {
            String startsAt = firstPickup.plus(day, ChronoUnit.DAYS).toString();
            postReservation(reservationRequest(CarType.SUV.name(), startsAt, 1)).expectStatus().isCreated();
        }
        String requestBody = reservationRequest(CarType.SUV, FLEET_CAPACITY);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isCreated();
    }

    @Test
    void given_one_car_left_when_two_requests_race_should_book_exactly_one_reservation()
        throws InterruptedException, ExecutionException, TimeoutException {
        // given
        CarType carType = CarType.SUV;
        String requestBody = reservationRequest(carType, RENTAL_DAYS);
        assertThat(jdbcTemplate.queryForObject("show transaction_isolation", String.class)).isEqualTo("repeatable read");
        createReservations(requestBody, FLEET_CAPACITY - 1);

        try (ExecutorService requests = Executors.newFixedThreadPool(2)) {
            // when
            List<Future<HttpStatusCode>> responses = transactions.execute(status -> {
                // This lock blocks booking lookups while allowing foreign-key checks to proceed.
                jdbcTemplate.queryForObject(
                    "select capacity from car_fleet where car_type = ? for no key update",
                    Integer.class, carType.name());
                Future<HttpStatusCode> first = requests.submit(() -> postReservationStatus(requestBody));
                Future<HttpStatusCode> second = requests.submit(() -> postReservationStatus(requestBody));
                await().atMost(LOCK_WAIT_TIMEOUT).untilAsserted(() -> assertThat(waitingForFleetLock()).isEqualTo(2));
                return List.of(first, second);
            });

            // then
            assertThat(List.of(
                responses.getFirst().get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                responses.getLast().get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(HttpStatus.CREATED, HttpStatus.CONFLICT);
            assertThat(jdbcTemplate.queryForObject(
                "select count(*) from car_reservation where car_type = ?", Integer.class, carType.name()))
                .isEqualTo(FLEET_CAPACITY);
        }
    }

    @Test
    void given_supported_car_type_without_a_fleet_when_reservation_is_requested_should_return_processing_failure() {
        // given
        CarType carType = CarType.VAN;
        String requestBody = reservationRequest(carType, RENTAL_DAYS);
        jdbcTemplate.update("delete from car_fleet where car_type = ?", carType.name());

        try {
            // when
            ResponseSpec response = postReservation(requestBody);

            // then
            response.expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.title").isEqualTo("Reservation processing failed");
        } finally {
            jdbcTemplate.update("insert into car_fleet (car_type, capacity) values (?, ?)", carType.name(), FLEET_CAPACITY);
        }
    }

    @Test
    void given_zero_rental_days_when_reservation_is_requested_should_return_bad_request() {
        // given
        String requestBody = reservationRequest(CarType.VAN, 0);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.9", "\"2\""})
    void given_non_integer_json_duration_when_reservation_is_requested_should_return_bad_request(String durationJson) {
        // given
        String requestBody = reservationRequest(CarType.VAN.name(), RENTAL_START, durationJson);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isBadRequest();
    }

    @Test
    void given_submicrosecond_pickup_when_reservation_is_requested_should_return_bad_request() {
        // given
        String requestBody = reservationRequest(CarType.SUV.name(), "2030-01-10T10:15:30.000000700Z", RENTAL_DAYS);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.detail").isEqualTo("Rental timestamps must use microsecond precision or coarser");
    }

    @Test
    void given_microsecond_handoff_when_a_booking_ends_at_pickup_should_allow_the_handoff() {
        // given
        String handoff = "2030-01-11T10:15:30.000001Z";
        createReservations(reservationRequest(CarType.SUV.name(), handoff, 1), FLEET_CAPACITY);
        String requestBody = reservationRequest(CarType.SUV.name(), "2030-01-10T10:15:30.000001Z", 1);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.endsAt").isEqualTo(handoff);
    }

    @ParameterizedTest
    @CsvSource({"2030-01-10T10:15:30Z, 200000000", "+294277-01-01T00:00:00Z, 1"})
    void given_rental_outside_the_supported_horizon_when_reservation_is_requested_should_return_bad_request(
        String startsAt, int numberOfDays) {
        // given
        String requestBody = reservationRequest(CarType.SUV.name(), startsAt, numberOfDays);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.detail").isEqualTo("Rental pickup and return must be on or before +294276-12-31T23:59:59.999999Z");
    }

    @Test
    void given_return_at_the_supported_horizon_when_reservation_is_requested_should_create_the_reservation() {
        // given
        String requestBody = reservationRequest(CarType.SUV.name(), "+294276-12-30T23:59:59.999999Z", 1);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.endsAt").isEqualTo("+294276-12-31T23:59:59.999999Z");
    }

    @Test
    void given_start_before_current_time_when_reservation_is_requested_should_return_bad_request() {
        // given
        String requestBody = reservationRequest(CarType.VAN.name(), "2029-12-31T23:59:59Z", RENTAL_DAYS);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isBadRequest()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.detail").isEqualTo("Rental start must be in the present or future");
    }

    @Test
    void given_unsupported_car_type_when_reservation_is_requested_should_return_bad_request() {
        // given
        String requestBody = reservationRequest(UNSUPPORTED_CAR_TYPE, RENTAL_DAYS);

        // when
        ResponseSpec response = postReservation(requestBody);

        // then
        response.expectStatus().isBadRequest();
    }

    private void createReservations(String requestBody, int numberOfReservations) {
        for (int reservationNumber = 0; reservationNumber < numberOfReservations; reservationNumber++) {
            postReservation(requestBody).expectStatus().isCreated();
        }
    }

    private Integer waitingForFleetLock() {
        return jdbcTemplate.queryForObject("""
            select count(*) from pg_stat_activity
            where datname = current_database()
              and wait_event_type = 'Lock'
              and query like '%from car_fleet%'
            """, Integer.class);
    }

    private HttpStatusCode postReservationStatus(String requestBody) {
        return postReservation(requestBody).returnResult(Void.class).getStatus();
    }

    private ResponseSpec postReservation(String requestBody) {
        return httpClient.post().uri(RESERVATIONS_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange();
    }

    private String reservationRequest(CarType carType, int numberOfDays) {
        return reservationRequest(carType.name(), numberOfDays);
    }

    private String reservationRequest(String carType, int numberOfDays) {
        return reservationRequest(carType, RENTAL_START, numberOfDays);
    }

    private String reservationRequest(String carType, String startsAt, int numberOfDays) {
        return reservationRequest(carType, startsAt, Integer.toString(numberOfDays));
    }

    private String reservationRequest(String carType, String startsAt, String durationJson) {
        return """
            {
              "carType": "%s",
              "startsAt": "%s",
              "numberOfDays": %s
            }
            """.formatted(carType, startsAt, durationJson);
    }
}
