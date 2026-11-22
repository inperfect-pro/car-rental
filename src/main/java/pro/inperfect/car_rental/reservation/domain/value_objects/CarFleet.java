package pro.inperfect.car_rental.reservation.domain.value_objects;

import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;

import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public record CarFleet(CarType carType, int capacity) {

    public CarFleet {
        if (carType == null) {
            throw new IllegalArgumentException("car type must not be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("fleet capacity must be positive");
        }
    }

    public static CarFleet create(CarType carType, int capacity) throws InvalidCarFleetException {
        try {
            return new CarFleet(carType, capacity);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCarFleetException(exception.getMessage(), exception);
        }
    }

    public void ensureAvailableFor(RentalPeriod requestedPeriod, List<RentalPeriod> bookedPeriods)
        throws InvalidCarFleetException, CarUnavailableException {
        if (requestedPeriod == null) {
            throw new InvalidCarFleetException("rental period must not be null");
        }
        if (bookedPeriods == null) {
            throw new InvalidCarFleetException("booked rental periods must not be null");
        }

        int occupied = 0;
        for (int change : occupancyChangesDuring(requestedPeriod, bookedPeriods).values()) {
            occupied += change;
            if (occupied >= capacity) {
                throw new CarUnavailableException(carType, requestedPeriod);
            }
        }
    }

    private NavigableMap<Instant, Integer> occupancyChangesDuring(
        RentalPeriod requestedPeriod, List<RentalPeriod> bookedPeriods) {
        NavigableMap<Instant, Integer> changes = new TreeMap<>();
        for (RentalPeriod bookedPeriod : bookedPeriods) {
            if (requestedPeriod.overlaps(bookedPeriod)) {
                Instant start = bookedPeriod.startsAt().isBefore(requestedPeriod.startsAt())
                    ? requestedPeriod.startsAt() : bookedPeriod.startsAt();
                Instant end = bookedPeriod.endsAt().isAfter(requestedPeriod.endsAt())
                    ? requestedPeriod.endsAt() : bookedPeriod.endsAt();
                // Combining changes at the same instant lets a return free capacity for a pickup.
                changes.merge(start, 1, Integer::sum);
                changes.merge(end, -1, Integer::sum);
            }
        }
        return changes;
    }
}
