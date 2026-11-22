package pro.inperfect.car_rental.reservation.adapter.out.persistence;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pro.inperfect.car_rental.base.AbstractWholeContextTest;
import pro.inperfect.car_rental.reservation.domain.Reservation;
import pro.inperfect.car_rental.reservation.domain.ReservationRepository;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;
import pro.inperfect.car_rental.reservation.domain.value_objects.ReservationId;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgresReservationRepositoryTest extends AbstractWholeContextTest {

    private final ReservationRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactions;

    @Autowired
    PostgresReservationRepositoryTest(
        ReservationRepository repository, JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Test
    void given_existing_reservation_when_the_same_id_is_added_should_reject_the_duplicate()
        throws ReservationException {
        // given
        ReservationId id = new ReservationId("duplicate-reservation-test");
        RentalPeriod period = RentalPeriod.startingAtForDays(Instant.parse("2030-01-10T10:15:30Z"), 2);
        Reservation original = Reservation.create(id, CarType.SEDAN, period);
        Reservation duplicate = Reservation.create(id, CarType.VAN, period);
        transactions.executeWithoutResult(status -> repository.add(original));

        try {
            // when
            ThrowingCallable addDuplicate = () -> transactions.executeWithoutResult(status -> repository.add(duplicate));

            // then
            assertThatThrownBy(addDuplicate).isInstanceOf(DataIntegrityViolationException.class);
            assertThat(jdbcTemplate.queryForObject(
                "select car_type from car_reservation where reservation_id = ?", String.class, id.value()))
                .isEqualTo(CarType.SEDAN.name());
        } finally {
            jdbcTemplate.update("delete from car_reservation where reservation_id = ?", id.value());
        }
    }
}
