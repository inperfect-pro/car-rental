package pro.inperfect.car_rental.reservation.domain.value_objects;

public record ReservationId(String value) {

    public ReservationId {
        if (value == null) {
            throw new IllegalArgumentException("reservation id must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("reservation id must not be blank");
        }
    }
}
