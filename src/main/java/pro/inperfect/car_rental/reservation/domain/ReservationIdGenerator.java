package pro.inperfect.car_rental.reservation.domain;

import pro.inperfect.car_rental.reservation.domain.value_objects.ReservationId;

public interface ReservationIdGenerator {

    ReservationId next();
}
