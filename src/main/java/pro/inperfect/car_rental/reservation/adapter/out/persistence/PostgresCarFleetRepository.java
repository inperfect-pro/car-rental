package pro.inperfect.car_rental.reservation.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import pro.inperfect.car_rental.reservation.domain.CarFleetRepository;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarFleet;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.util.Optional;

@RequiredArgsConstructor
public class PostgresCarFleetRepository implements CarFleetRepository {

    private final SpringDataCarFleetRepository springDataCarFleetRepository;

    @Override
    public Optional<CarFleet> findByCarTypeForUpdate(CarType carType) throws InvalidCarFleetException {
        Optional<CarFleetJpaEntity> result = springDataCarFleetRepository.findByCarTypeForUpdate(carType);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        CarFleetJpaEntity carFleet = result.get();
        return Optional.of(CarFleet.create(carFleet.getCarType(), carFleet.getCapacity()));
    }
}
