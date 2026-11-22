package pro.inperfect.car_rental.reservation.domain.exceptions;

public final class ReservationStartsInPastException extends ReservationException {

    public ReservationStartsInPastException() {
        super("Rental start must be in the present or future");
    }
}
