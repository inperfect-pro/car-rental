package pro.inperfect.car_rental.reservation.adapter.in.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import pro.inperfect.car_rental.reservation.application.CreateReservationCommand;

import java.time.OffsetDateTime;

public record CreateReservationRequest(
    @NotBlank
    @Pattern(regexp = CarTypeMapper.CAR_TYPE_PATTERN, message = CarTypeMapper.INVALID_CAR_TYPE_MESSAGE)
    String carType,
    @NotNull OffsetDateTime startsAt,
    @Positive int numberOfDays) {

    public CreateReservationCommand toCommand() {
        return new CreateReservationCommand(CarTypeMapper.toDomain(carType), startsAt.toInstant(), numberOfDays);
    }
}
