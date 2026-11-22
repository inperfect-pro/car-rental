package pro.inperfect.car_rental.reservation.adapter.out.identifier;

import pro.inperfect.car_rental.reservation.domain.ReservationIdGenerator;
import pro.inperfect.car_rental.reservation.domain.value_objects.ReservationId;

import java.util.UUID;

public class UuidReservationIdGenerator implements ReservationIdGenerator {

    @Override
    public ReservationId next() {
        return new ReservationId(UUID.randomUUID().toString());
    }
}
