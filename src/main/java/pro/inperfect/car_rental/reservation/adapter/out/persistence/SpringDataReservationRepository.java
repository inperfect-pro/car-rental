package pro.inperfect.car_rental.reservation.adapter.out.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.time.Instant;
import java.util.List;

public interface SpringDataReservationRepository extends Repository<ReservationJpaEntity, String> {

    @Query("""
        select reservation
        from ReservationJpaEntity reservation
        where reservation.carType = :carType
          and reservation.startsAt < :endsAt
          and reservation.endsAt > :startsAt
        """)
    List<ReservationJpaEntity> findOverlappingReservations(CarType carType, Instant startsAt, Instant endsAt);
}
