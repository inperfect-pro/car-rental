package pro.inperfect.car_rental.reservation.domain.value_objects;

import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record RentalPeriod(Instant startsAt, Instant endsAt) {

    private static final Instant LATEST_SUPPORTED_TIME = Instant.parse("+294276-12-31T23:59:59.999999Z");
    private static final String UNSUPPORTED_TIME_MESSAGE = "Rental pickup and return must be on or before "
        + LATEST_SUPPORTED_TIME;

    public RentalPeriod {
        if (startsAt == null) {
            throw new IllegalArgumentException("rental start must not be null");
        }
        if (endsAt == null) {
            throw new IllegalArgumentException("rental end must not be null");
        }
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("rental end must be after rental start");
        }
        if (endsAt.isAfter(LATEST_SUPPORTED_TIME)) {
            throw new IllegalArgumentException(UNSUPPORTED_TIME_MESSAGE);
        }
        if (!hasMicrosecondPrecision(startsAt) || !hasMicrosecondPrecision(endsAt)) {
            throw new IllegalArgumentException("Rental timestamps must use microsecond precision or coarser");
        }
    }

    public static RentalPeriod startingAtForDays(Instant startsAt, int numberOfDays) throws InvalidRentalPeriodException {
        if (startsAt == null) {
            throw new InvalidRentalPeriodException("rental start must not be null");
        }
        if (numberOfDays <= 0) {
            throw new InvalidRentalPeriodException("number of rental days must be positive");
        }
        if (numberOfDays > ChronoUnit.DAYS.between(startsAt, LATEST_SUPPORTED_TIME)) {
            throw new InvalidRentalPeriodException(UNSUPPORTED_TIME_MESSAGE);
        }
        try {
            return new RentalPeriod(startsAt, startsAt.plus(numberOfDays, ChronoUnit.DAYS));
        } catch (IllegalArgumentException exception) {
            throw new InvalidRentalPeriodException(exception.getMessage(), exception);
        }
    }

    public boolean overlaps(RentalPeriod other) {
        if (other == null) {
            throw new IllegalArgumentException("rental period to compare must not be null");
        }
        return startsAt.isBefore(other.endsAt) && other.startsAt.isBefore(endsAt);
    }

    private static boolean hasMicrosecondPrecision(Instant time) {
        return time.equals(time.truncatedTo(ChronoUnit.MICROS));
    }
}
