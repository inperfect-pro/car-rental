package pro.inperfect.car_rental.reservation.domain;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarFleetNotConfiguredException;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationStartsInPastException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarFleet;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;
import pro.inperfect.car_rental.reservation.domain.value_objects.ReservationId;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Instant START = Instant.parse("2030-01-10T10:15:30Z");
    private static final Instant END = Instant.parse("2030-01-12T10:15:30Z");
    private static final Clock CLOCK = Clock.fixed(START, ZoneOffset.UTC);
    private static final int RENTAL_DAYS = 2;
    private static final ReservationId RESERVATION_ID = new ReservationId("reservation-123");

    @Mock
    private CarFleetRepository carFleetRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationIdGenerator reservationIdGenerator;

    @Test
    void given_available_car_type_when_reservation_is_created_should_persist_requested_rental_period()
        throws ReservationException {
        // given
        ReservationService service = new ReservationService(
            carFleetRepository, reservationRepository, reservationIdGenerator, CLOCK);
        RentalPeriod expectedPeriod = new RentalPeriod(START, END);
        Reservation expectedReservation = Reservation.create(RESERVATION_ID, CarType.SEDAN, expectedPeriod);
        given(carFleetRepository.findByCarTypeForUpdate(CarType.SEDAN))
            .willReturn(Optional.of(CarFleet.create(CarType.SEDAN, 1)));
        given(reservationRepository.findOverlappingPeriods(CarType.SEDAN, expectedPeriod)).willReturn(List.of());
        given(reservationIdGenerator.next()).willReturn(RESERVATION_ID);

        // when
        Reservation reservation = service.reserve(CarType.SEDAN, START, RENTAL_DAYS);

        // then
        assertThat(reservation).isEqualTo(expectedReservation);
        then(reservationRepository).should().add(expectedReservation);
    }

    @Test
    void given_car_type_at_capacity_when_reservation_is_created_should_reject_the_request()
        throws InvalidCarFleetException {
        // given
        ReservationService service = new ReservationService(
            carFleetRepository, reservationRepository, reservationIdGenerator, CLOCK);
        RentalPeriod expectedPeriod = new RentalPeriod(START, END);
        given(carFleetRepository.findByCarTypeForUpdate(CarType.VAN))
            .willReturn(Optional.of(CarFleet.create(CarType.VAN, 2)));
        given(reservationRepository.findOverlappingPeriods(CarType.VAN, expectedPeriod))
            .willReturn(List.of(expectedPeriod, expectedPeriod));

        // when
        ThrowingCallable reservationCreation = () -> service.reserve(CarType.VAN, START, RENTAL_DAYS);

        // then
        assertThatThrownBy(reservationCreation).isInstanceOf(CarUnavailableException.class);
        then(reservationRepository).should(never()).add(any());
        verifyNoInteractions(reservationIdGenerator);
    }

    @Test
    void given_car_type_without_a_fleet_when_reservation_is_created_should_report_missing_fleet()
        throws InvalidCarFleetException {
        // given
        ReservationService service = new ReservationService(
            carFleetRepository, reservationRepository, reservationIdGenerator, CLOCK);
        given(carFleetRepository.findByCarTypeForUpdate(CarType.SUV)).willReturn(Optional.empty());

        // when
        ThrowingCallable reservationCreation = () -> service.reserve(CarType.SUV, START, RENTAL_DAYS);

        // then
        assertThatThrownBy(reservationCreation)
            .isInstanceOf(CarFleetNotConfiguredException.class)
            .hasMessage("No fleet is configured for car type SUV");
        verifyNoInteractions(reservationRepository, reservationIdGenerator);
    }

    @Test
    void given_start_before_current_time_when_reservation_is_created_should_reject_the_request() {
        // given
        ReservationService service = new ReservationService(
            carFleetRepository, reservationRepository, reservationIdGenerator, CLOCK);
        Instant pastStart = START.minusSeconds(1);

        // when
        ThrowingCallable reservationCreation = () -> service.reserve(CarType.SEDAN, pastStart, RENTAL_DAYS);

        // then
        assertThatThrownBy(reservationCreation).isInstanceOf(ReservationStartsInPastException.class);
        verifyNoInteractions(carFleetRepository, reservationRepository, reservationIdGenerator);
    }
}
