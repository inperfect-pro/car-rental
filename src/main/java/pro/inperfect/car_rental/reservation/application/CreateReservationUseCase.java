package pro.inperfect.car_rental.reservation.application;

import pro.inperfect.car_rental.reservation.domain.Reservation;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarFleetNotConfiguredException;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidReservationException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationStartsInPastException;

public interface CreateReservationUseCase {

    Reservation create(CreateReservationCommand command)
        throws CarFleetNotConfiguredException, CarUnavailableException, InvalidCarFleetException,
        InvalidReservationException, InvalidRentalPeriodException, ReservationStartsInPastException;
}
