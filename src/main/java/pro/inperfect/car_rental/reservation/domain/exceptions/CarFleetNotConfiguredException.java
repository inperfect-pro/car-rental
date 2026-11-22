package pro.inperfect.car_rental.reservation.domain.exceptions;

import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

public final class CarFleetNotConfiguredException extends ReservationException {

    public CarFleetNotConfiguredException(CarType carType) {
        super("No fleet is configured for car type %s".formatted(carType));
    }
}
