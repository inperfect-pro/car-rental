package pro.inperfect.car_rental.reservation.domain;

import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarFleet;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.util.Optional;

public interface CarFleetRepository {

    Optional<CarFleet> findByCarTypeForUpdate(CarType carType) throws InvalidCarFleetException;
}
