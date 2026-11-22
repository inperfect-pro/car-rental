package pro.inperfect.car_rental.reservation.domain.exceptions;

public final class InvalidRentalPeriodException extends ReservationException {

    public InvalidRentalPeriodException(String message) {
        super(message);
    }

    public InvalidRentalPeriodException(String message, Throwable cause) {
        super(message, cause);
    }
}
