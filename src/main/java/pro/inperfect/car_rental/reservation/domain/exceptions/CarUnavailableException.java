package pro.inperfect.car_rental.reservation.domain.exceptions;

import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;

import java.util.Objects;

public final class CarUnavailableException extends ReservationException {

    private final CarType carType;
    private final RentalPeriod rentalPeriod;

    public CarUnavailableException(CarType carType, RentalPeriod rentalPeriod) {
        super("No %s is available for the requested rental period".formatted(carType));
        this.carType = Objects.requireNonNull(carType, "car type must not be null");
        this.rentalPeriod = Objects.requireNonNull(rentalPeriod, "rental period must not be null");
    }

    public CarType carType() {
        return carType;
    }

    public RentalPeriod rentalPeriod() {
        return rentalPeriod;
    }
}
