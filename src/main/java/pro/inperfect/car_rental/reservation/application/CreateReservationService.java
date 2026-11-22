package pro.inperfect.car_rental.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pro.inperfect.car_rental.reservation.domain.Reservation;
import pro.inperfect.car_rental.reservation.domain.ReservationService;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarFleetNotConfiguredException;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidReservationException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationStartsInPastException;

@RequiredArgsConstructor
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationService reservationService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = ReservationException.class)
    public Reservation create(CreateReservationCommand command)
        throws CarFleetNotConfiguredException, CarUnavailableException, InvalidCarFleetException,
        InvalidReservationException, InvalidRentalPeriodException, ReservationStartsInPastException {
        return reservationService.reserve(command.carType(), command.startsAt(), command.numberOfDays());
    }
}
