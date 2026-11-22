package pro.inperfect.car_rental.reservation.adapter.in.http.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pro.inperfect.car_rental.reservation.domain.value_objects.CarType;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CarTypeMapper {

    static final String SEDAN = "SEDAN";
    static final String SUV = "SUV";
    static final String VAN = "VAN";
    static final String CAR_TYPE_PATTERN = SEDAN + "|" + SUV + "|" + VAN;
    static final String INVALID_CAR_TYPE_MESSAGE = "car type must be one of SEDAN, SUV, VAN";

    static CarType toDomain(String carType) {
        if (carType == null) {
            throw new IllegalArgumentException(INVALID_CAR_TYPE_MESSAGE);
        }
        return switch (carType) {
            case SEDAN -> CarType.SEDAN;
            case SUV -> CarType.SUV;
            case VAN -> CarType.VAN;
            default -> throw new IllegalArgumentException(INVALID_CAR_TYPE_MESSAGE);
        };
    }

    public static String fromDomain(CarType carType) {
        Objects.requireNonNull(carType, "car type must not be null");
        return switch (carType) {
            case SEDAN -> SEDAN;
            case SUV -> SUV;
            case VAN -> VAN;
        };
    }
}
