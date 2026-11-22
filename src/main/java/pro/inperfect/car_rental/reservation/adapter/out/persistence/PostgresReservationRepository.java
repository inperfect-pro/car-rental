package pro.inperfect.car_rental.reservation.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import pro.inperfect.car_rental.reservation.domain.Reservation;
import pro.inperfect.car_rental.reservation.domain.ReservationRepository;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;

import java.util.List;

@RequiredArgsConstructor
public class PostgresReservationRepository implements ReservationRepository {

    private final SpringDataReservationRepository springDataReservationRepository;
    private final EntityManager entityManager;

    @Override
    public List<RentalPeriod> findOverlappingPeriods(CarType carType, RentalPeriod rentalPeriod) {
        return springDataReservationRepository.findOverlappingReservations(
                carType, rentalPeriod.startsAt(), rentalPeriod.endsAt())
            .stream()
            .map(reservation -> new RentalPeriod(reservation.getStartsAt(), reservation.getEndsAt()))
            .toList();
    }

    @Override
    public void add(Reservation reservation) {
        entityManager.persist(new ReservationJpaEntity(
            reservation.id().value(),
            reservation.carType(),
            reservation.rentalPeriod().startsAt(),
            reservation.rentalPeriod().endsAt()));
    }
}
