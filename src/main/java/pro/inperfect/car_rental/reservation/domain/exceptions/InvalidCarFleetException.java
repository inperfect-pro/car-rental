package pro.inperfect.car_rental.reservation.domain.exceptions;

public final class InvalidCarFleetException extends ReservationException {

    public InvalidCarFleetException(String message) {
        super(message);
    }

    public InvalidCarFleetException(String message, Throwable cause) {
        super(message, cause);
    }
}
