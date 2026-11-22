package pro.inperfect.car_rental.reservation.adapter.in.http;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.inperfect.car_rental.reservation.adapter.in.http.dto.CreateReservationRequest;
import pro.inperfect.car_rental.reservation.adapter.in.http.dto.CreateReservationResponse;
import pro.inperfect.car_rental.reservation.application.CreateReservationCommand;
import pro.inperfect.car_rental.reservation.application.CreateReservationUseCase;
import pro.inperfect.car_rental.reservation.domain.Reservation;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarFleetNotConfiguredException;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidCarFleetException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidRentalPeriodException;
import pro.inperfect.car_rental.reservation.domain.exceptions.InvalidReservationException;
import pro.inperfect.car_rental.reservation.domain.exceptions.ReservationStartsInPastException;

import java.net.URI;

@RestController
@RequestMapping(ReservationController.RESERVATIONS_PATH)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class ReservationController {

    static final String RESERVATIONS_PATH = "/api/reservations";

    private final CreateReservationUseCase createReservationUseCase;

    @PostMapping
    ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        CreateReservationCommand command;
        try {
            command = request.toCommand();
        } catch (IllegalArgumentException exception) {
            return ReservationErrorMapper.invalidRequest(exception.getMessage());
        }

        Reservation reservation;
        try {
            reservation = createReservationUseCase.create(command);
        } catch (CarUnavailableException exception) {
            return ReservationErrorMapper.carUnavailable(exception);
        } catch (ReservationStartsInPastException | InvalidRentalPeriodException exception) {
            return ReservationErrorMapper.invalidRequest(exception.getMessage());
        } catch (CarFleetNotConfiguredException | InvalidCarFleetException | InvalidReservationException exception) {
            log.error("Reservation processing failed", exception);
            return ReservationErrorMapper.processingFailure();
        }

        URI location = URI.create(RESERVATIONS_PATH + "/" + reservation.id().value());
        return ResponseEntity.created(location)
            .body(CreateReservationResponse.from(reservation));
    }
}
