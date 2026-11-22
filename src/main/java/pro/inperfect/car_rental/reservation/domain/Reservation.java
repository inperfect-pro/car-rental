package pro.inperfect.car_rental.reservation.domain;

import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidReservationException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;
import pro.inperfect.car_rental.reservation.domain.value_objects.ReservationId;

import java.util.Objects;

public record Reservation(ReservationId id, CarType carType, RentalPeriod rentalPeriod) {

    public Reservation {
        Objects.requireNonNull(id, "reservation id must not be null");
        Objects.requireNonNull(carType, "car type must not be null");
        Objects.requireNonNull(rentalPeriod, "rental period must not be null");
    }

    public static Reservation create(ReservationId id, CarType carType, RentalPeriod rentalPeriod)
        throws InvalidReservationException {
        try {
            return new Reservation(id, carType, rentalPeriod);
        } catch (NullPointerException exception) {
            throw new InvalidReservationException(exception.getMessage(), exception);
        }
    }
}
