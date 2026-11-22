package pro.inperfect.car_rental.reservation.adapter.in.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import pro.inperfect.car_rental.reservation.adapter.in.http.dto.CarTypeMapper;
import pro.inperfect.car_rental.reservation.domain.exceptions.CarUnavailableException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReservationErrorMapper {

    private static final String CAR_UNAVAILABLE_TITLE = "Car type unavailable";
    private static final String INVALID_RESERVATION_REQUEST_TITLE = "Invalid reservation request";
    private static final String RESERVATION_PROCESSING_FAILURE_TITLE = "Reservation processing failed";

    static ResponseEntity<ProblemDetail> carUnavailable(CarUnavailableException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle(CAR_UNAVAILABLE_TITLE);
        problemDetail.setProperty("carType", CarTypeMapper.fromDomain(exception.carType()));
        problemDetail.setProperty("startsAt", exception.rentalPeriod().startsAt());
        problemDetail.setProperty("endsAt", exception.rentalPeriod().endsAt());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    static ResponseEntity<ProblemDetail> invalidRequest(String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle(INVALID_RESERVATION_REQUEST_TITLE);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    static ResponseEntity<ProblemDetail> processingFailure() {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle(RESERVATION_PROCESSING_FAILURE_TITLE);
        return ResponseEntity.internalServerError().body(problemDetail);
    }
}
