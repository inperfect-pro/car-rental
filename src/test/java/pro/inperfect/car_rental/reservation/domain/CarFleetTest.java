package pro.inperfect.car_rental.reservation.domain;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarFleet;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarFleetTest {

    private static final Instant START = Instant.parse("2030-01-10T10:15:30Z");

    @Test
    void given_fleet_at_capacity_when_availability_is_checked_should_report_car_unavailable()
        throws InvalidCarFleetException, InvalidRentalPeriodException {
        // given
        CarFleet carFleet = CarFleet.create(CarType.SEDAN, 1);
        RentalPeriod rentalPeriod = RentalPeriod.startingAtForDays(START, 2);

        // when
        ThrowingCallable availabilityCheck = () -> carFleet.ensureAvailableFor(rentalPeriod, List.of(rentalPeriod));

        // then
        assertThatThrownBy(availabilityCheck)
            .isInstanceOf(CarUnavailableException.class)
            .hasMessage("No SEDAN is available for the requested rental period");
    }

    @Test
    void given_back_to_back_bookings_when_availability_is_checked_should_allow_the_rental()
        throws InvalidCarFleetException, InvalidRentalPeriodException {
        // given
        CarFleet carFleet = CarFleet.create(CarType.SUV, 2);
        RentalPeriod first = RentalPeriod.startingAtForDays(START, 1);
        RentalPeriod second = RentalPeriod.startingAtForDays(first.endsAt(), 1);
        RentalPeriod requested = new RentalPeriod(first.startsAt(), second.endsAt());

        // when
        ThrowingCallable availabilityCheck = () -> carFleet.ensureAvailableFor(requested, List.of(second, first));

        // then
        assertThatCode(availabilityCheck).doesNotThrowAnyException();
    }

    @Test
    void given_fleet_reaches_capacity_mid_rental_when_availability_is_checked_should_reject_the_rental()
        throws InvalidCarFleetException, InvalidRentalPeriodException {
        // given
        CarFleet carFleet = CarFleet.create(CarType.SUV, 2);
        RentalPeriod first = RentalPeriod.startingAtForDays(START, 3);
        RentalPeriod requested = RentalPeriod.startingAtForDays(START.plusSeconds(1), 3);
        RentalPeriod second = new RentalPeriod(first.endsAt().minusSeconds(1), requested.endsAt().plusSeconds(1));

        // when
        ThrowingCallable availabilityCheck = () -> carFleet.ensureAvailableFor(requested, List.of(first, second));

        // then
        assertThatThrownBy(availabilityCheck).isInstanceOf(CarUnavailableException.class);
    }

    @Test
    void given_non_positive_capacity_when_fleet_is_created_should_reject_invalid_fleet() {
        // given
        int nonPositiveCapacity = 0;

        // when
        ThrowingCallable fleetCreation = () -> CarFleet.create(CarType.SEDAN, nonPositiveCapacity);

        // then
        assertThatThrownBy(fleetCreation)
            .isInstanceOf(InvalidCarFleetException.class)
            .hasMessage("fleet capacity must be positive");
    }

    @Test
    void given_missing_car_type_when_record_constructor_is_called_should_reject_invalid_fleet() {
        // given
        int positiveCapacity = 1;

        // when
        ThrowingCallable fleetCreation = () -> new CarFleet(null, positiveCapacity);

        // then
        assertThatThrownBy(fleetCreation)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("car type must not be null");
    }
}
