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

@Entity
@Table(name = "car_fleet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarFleetJpaEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false, updatable = false)
    private CarType carType;

    @Column(name = "capacity", nullable = false)
    private int capacity;
}
