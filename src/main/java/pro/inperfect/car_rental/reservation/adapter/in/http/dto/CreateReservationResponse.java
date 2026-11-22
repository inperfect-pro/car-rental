package pro.inperfect.car_rental.reservation.adapter.in.http.dto;

import pro.inperfect.car_rental.reservation.domain.Reservation;

import java.time.Instant;

public record CreateReservationResponse(
    String reservationId,
    String carType,
    Instant startsAt,
    Instant endsAt) {

    public static CreateReservationResponse from(Reservation reservation) {
        return new CreateReservationResponse(
            reservation.id().value(),
            CarTypeMapper.fromDomain(reservation.carType()),
            reservation.rentalPeriod().startsAt(),
            reservation.rentalPeriod().endsAt());
    }
}
