package pro.inperfect.car_rental.reservation.domain;

import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;

import java.util.List;

public interface ReservationRepository {

    List<RentalPeriod> findOverlappingPeriods(CarType carType, RentalPeriod rentalPeriod);

    void add(Reservation reservation);
}
