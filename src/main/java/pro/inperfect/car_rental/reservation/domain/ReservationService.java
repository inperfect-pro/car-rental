package pro.inperfect.car_rental.reservation.domain;

import lombok.RequiredArgsConstructor;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarFleetNotConfiguredException;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidReservationException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationStartsInPastException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarFleet;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public final class ReservationService {

    private final CarFleetRepository carFleetRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationIdGenerator reservationIdGenerator;
    private final Clock clock;

    public Reservation reserve(CarType carType, Instant startsAt, int numberOfDays)
        throws CarFleetNotConfiguredException, CarUnavailableException, InvalidCarFleetException,
        InvalidReservationException, InvalidRentalPeriodException, ReservationStartsInPastException {
        RentalPeriod rentalPeriod = RentalPeriod.startingAtForDays(startsAt, numberOfDays);
        if (rentalPeriod.startsAt().isBefore(clock.instant())) {
            throw new ReservationStartsInPastException();
        }

        CarFleet carFleet = carFleetRepository.findByCarTypeForUpdate(carType)
            .orElseThrow(() -> new CarFleetNotConfiguredException(carType));
        List<RentalPeriod> overlappingPeriods = reservationRepository.findOverlappingPeriods(carType, rentalPeriod);
        carFleet.ensureAvailableFor(rentalPeriod, overlappingPeriods);

        Reservation reservation = Reservation.create(reservationIdGenerator.next(), carType, rentalPeriod);
        reservationRepository.add(reservation);
        return reservation;
    }
}
