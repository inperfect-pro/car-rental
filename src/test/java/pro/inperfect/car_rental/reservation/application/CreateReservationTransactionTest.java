package pro.inperfect.car_rental.reservation.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pro.inperfect.car_rental.base.AbstractWholeContextTest;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CreateReservationTransactionTest extends AbstractWholeContextTest {

    private final CreateReservationUseCase createReservation;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    CreateReservationTransactionTest(
        CreateReservationUseCase createReservation, PlatformTransactionManager transactionManager) {
        this.createReservation = createReservation;
        this.transactionManager = transactionManager;
    }

    @Test
    void given_repeatable_read_transaction_when_reservation_is_requested_should_reject_existing_transaction() {
        // given
        TransactionTemplate existingTransaction = new TransactionTemplate(transactionManager);
        existingTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        CreateReservationCommand command = new CreateReservationCommand(CarType.SUV, Instant.parse("2030-02-01T10:00:00Z"), 1);

        // when
        Throwable failure = existingTransaction.execute(status -> {
            status.setRollbackOnly();
            return catchThrowable(() -> createReservation.create(command));
        });

        // then
        assertThat(failure).isInstanceOf(IllegalTransactionStateException.class).hasMessageContaining("isolation");
    }
}
