package pro.inperfect.car_rental.reservation.domain.exceptions;

public abstract class ReservationException extends Exception {

    ReservationException(String message) {
        super(message);
    }

    ReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
