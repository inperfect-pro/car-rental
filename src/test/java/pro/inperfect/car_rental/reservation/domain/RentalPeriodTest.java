package pro.inperfect.car_rental.reservation.domain;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalPeriodTest {

    private static final Instant START = Instant.parse("2030-01-10T10:15:30Z");

    @Test
    void given_adjacent_rental_periods_when_overlap_is_checked_should_report_no_overlap()
        throws InvalidRentalPeriodException {
        // given
        RentalPeriod firstPeriod = RentalPeriod.startingAtForDays(START, 2);
        RentalPeriod adjacentPeriod = RentalPeriod.startingAtForDays(firstPeriod.endsAt(), 1);

        // when
        boolean overlaps = firstPeriod.overlaps(adjacentPeriod);

        // then
        assertThat(overlaps).isFalse();
    }

    @Test
    void given_non_positive_rental_duration_when_period_is_created_should_reject_it() {
        // given
        int nonPositiveNumberOfDays = 0;

        // when
        ThrowingCallable creation = () ->
            RentalPeriod.startingAtForDays(START, nonPositiveNumberOfDays);

        // then
        assertThatThrownBy(creation)
            .isInstanceOf(InvalidRentalPeriodException.class)
            .hasMessage("number of rental days must be positive");
    }

    @Test
    void given_missing_rental_start_when_period_is_created_should_reject_it() {
        // given
        Instant missingRentalStart = null;

        // when
        ThrowingCallable creation = () -> RentalPeriod.startingAtForDays(missingRentalStart, 2);

        // then
        assertThatThrownBy(creation)
            .isInstanceOf(InvalidRentalPeriodException.class)
            .hasMessage("rental start must not be null");
    }

    @Test
    void given_instant_that_cannot_be_extended_when_period_is_created_should_report_invalid_period() {
        // given
        Instant unsupportedStart = Instant.MAX;

        // when
        ThrowingCallable creation = () -> RentalPeriod.startingAtForDays(unsupportedStart, 1);

        // then
        assertThatThrownBy(creation)
            .isInstanceOf(InvalidRentalPeriodException.class)
            .hasMessageStartingWith("Rental pickup and return must be on or before");
    }
}
