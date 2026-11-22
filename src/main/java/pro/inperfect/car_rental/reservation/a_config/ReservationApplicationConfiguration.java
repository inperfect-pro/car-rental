package pro.inperfect.car_rental.reservation.a_config;

import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import pro.inperfect.car_rental.reservation.application.CreateReservationService;
import pro.inperfect.car_rental.reservation.application.CreateReservationUseCase;
import pro.inperfect.car_rental.reservation.domain.ReservationService;

@Configuration
class ReservationApplicationConfiguration {

    @Bean
    TransactionManagerCustomizer<JpaTransactionManager> validateExistingTransactionSettings() {
        return transactionManager -> transactionManager.setValidateExistingTransaction(true);
    }

    @Bean
    CreateReservationUseCase createReservationService(ReservationService reservationService) {
        return new CreateReservationService(reservationService);
    }
}
