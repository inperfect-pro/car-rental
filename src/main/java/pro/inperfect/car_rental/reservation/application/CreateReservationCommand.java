package pro.inperfect.car_rental.reservation.application;

import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.time.Instant;
import java.util.Objects;

public record CreateReservationCommand(CarType carType, Instant startsAt, int numberOfDays) {

    public CreateReservationCommand {
        Objects.requireNonNull(carType, "car type must not be null");
        Objects.requireNonNull(startsAt, "rental start must not be null");
        if (numberOfDays <= 0) {
            throw new IllegalArgumentException("number of rental days must be positive");
        }
    }
}
