package pro.inperfect.car_rental.reservation.domain.exceptions;

public final class InvalidReservationException extends ReservationException {

    public InvalidReservationException(String message) {
        super(message);
    }

    public InvalidReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
