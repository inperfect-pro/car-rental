package pro.inperfect.car_rental.reservation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.time.Instant;

@Entity
@Table(name = "car_reservation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationJpaEntity {

    @Id
    @Column(name = "reservation_id", nullable = false, updatable = false, length = 36)
    private String reservationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false, updatable = false)
    private CarType carType;

    @Column(name = "starts_at", nullable = false, updatable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false, updatable = false)
    private Instant endsAt;
}
