package pro.inperfect.car_rental.reservation.domain;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidReservationException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;
import pro.inperfect.car_rental.reservation.domain.value_objects.RentalPeriod;
import pro.inperfect.car_rental.reservation.domain.value_objects.ReservationId;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    void given_missing_rental_period_when_reservation_is_created_should_reject_invalid_reservation() {
        // given
        ReservationId reservationId = new ReservationId("reservation-123");

        // when
        ThrowingCallable reservationCreation = () -> Reservation.create(reservationId, CarType.SEDAN, null);

        // then
        assertThatThrownBy(reservationCreation)
            .isInstanceOf(InvalidReservationException.class)
            .hasMessage("rental period must not be null");
    }

    @Test
    void given_missing_id_when_record_constructor_is_called_should_reject_invalid_reservation()
        throws InvalidRentalPeriodException {
        // given
        RentalPeriod rentalPeriod = RentalPeriod.startingAtForDays(Instant.EPOCH, 1);

        // when
        ThrowingCallable reservationCreation = () -> new Reservation(null, CarType.SEDAN, rentalPeriod);

        // then
        assertThatThrownBy(reservationCreation)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("reservation id must not be null");
    }
}
