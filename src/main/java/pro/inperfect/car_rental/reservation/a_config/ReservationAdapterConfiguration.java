package pro.inperfect.car_rental.reservation.a_config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import pro.inperfect.car_rental.reservation.adapter.out.identifier.UuidReservationIdGenerator;
import pro.inperfect.car_rental.reservation.adapter.out.persistence.PostgresCarFleetRepository;
import pro.inperfect.car_rental.reservation.adapter.out.persistence.PostgresReservationRepository;
import pro.inperfect.car_rental.reservation.adapter.out.persistence.SpringDataCarFleetRepository;
import pro.inperfect.car_rental.reservation.adapter.out.persistence.SpringDataReservationRepository;
import pro.inperfect.car_rental.reservation.domain.CarFleetRepository;
import pro.inperfect.car_rental.reservation.domain.ReservationIdGenerator;
import pro.inperfect.car_rental.reservation.domain.ReservationRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = SpringDataCarFleetRepository.class)
class ReservationAdapterConfiguration {

    @Bean
    CarFleetRepository postgresCarFleetRepository(SpringDataCarFleetRepository springDataCarFleetRepository) {
        return new PostgresCarFleetRepository(springDataCarFleetRepository);
    }

    @Bean
    ReservationRepository postgresReservationRepository(
        SpringDataReservationRepository springDataReservationRepository, EntityManagerFactory entityManagerFactory) {
        return new PostgresReservationRepository(
            springDataReservationRepository, SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory));
    }

    @Bean
    ReservationIdGenerator uuidReservationIdGenerator() {
        return new UuidReservationIdGenerator();
    }
}
