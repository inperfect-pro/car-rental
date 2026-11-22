package pro.inperfect.car_rental.reservation.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.util.Optional;

public interface SpringDataCarFleetRepository extends Repository<CarFleetJpaEntity, CarType> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fleet from CarFleetJpaEntity fleet where fleet.carType = :carType")
    Optional<CarFleetJpaEntity> findByCarTypeForUpdate(CarType carType);
}
