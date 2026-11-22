package pro.inperfect.car_rental.reservation.a_config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.inperfect.car_rental.reservation.domain.CarFleetRepository;
import pro.inperfect.car_rental.reservation.domain.ReservationIdGenerator;
import pro.inperfect.car_rental.reservation.domain.ReservationRepository;
import pro.inperfect.car_rental.reservation.domain.ReservationService;

import java.time.Clock;

@Configuration
class ReservationDomainConfiguration {

    @Bean
    ReservationService reservationService(
        CarFleetRepository carFleetRepository,
        ReservationRepository reservationRepository,
        ReservationIdGenerator reservationIdGenerator,
        Clock clock) {
        return new ReservationService(carFleetRepository, reservationRepository, reservationIdGenerator, clock);
    }

    @Bean
    Clock reservationClock() {
        return Clock.systemUTC();
    }
}
