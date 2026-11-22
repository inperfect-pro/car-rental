# Car Rental

Small Spring Boot 4 service that creates time-bounded reservations for `SEDAN`, `SUV`, and `VAN` fleets.

## Run

Docker Compose supplies PostgreSQL for local execution:

```powershell
.\gradlew.bat bootRun
```

## Create a reservation

```http
POST /api/reservations
Content-Type: application/json

{
  "carType": "SUV",
  "startsAt": "2030-01-10T10:15:30Z",
  "numberOfDays": 2
}
```

The response is `201 Created` with the reservation ID and calculated end instant. A period with no remaining capacity returns `409 Conflict`.
`numberOfDays` must be a positive JSON integer, with 24 hours per day. Pickup must be in the present or future. Pickup and return use at most
microsecond precision and must fall on or before `+294276-12-31T23:59:59.999999Z`, the last finite PostgreSQL timestamp. Requests outside those
bounds return `400 Bad Request`.

## Verification

```powershell
.\gradlew.bat test
```

Domain unit tests cover rental-period and availability rules. Component tests use real HTTP and a pinned PostgreSQL Testcontainer with Flyway
migrations. A fixed clock keeps booking dates repeatable, and database-backed tests take an exclusive JUnit lock around shared fixtures.

## Focus areas and trade-offs

### Focus areas

- **Overlapping bookings.** Availability depends on peak simultaneous occupancy during the requested rental. Three back-to-back reservations use
  one of the three cars at any instant, so another car can be booked across all three periods. A return frees capacity for a pickup at the same
  instant. Domain tests cover this handoff and a fleet that reaches capacity partway through a rental. An HTTP test covers the staggered bookings.
- **The race for the last car.** The booking transaction locks the fleet row before reading availability and inserting a reservation. The use case
  requires Read Committed and rejects an existing transaction with incompatible settings. The HTTP race test makes both requests wait on the same
  row and verifies one `201 Created`, one `409 Conflict`, and exactly one new row, even when the connection pool defaults to Repeatable Read.
- **Project structure and Clean Architecture.** The domain owns booking rules and repository interfaces. `Reservation` is an immutable entity with
  an ID, and `RentalPeriod` is a value object. The application layer owns the use case and its transaction. The adapters handle HTTP and PostgreSQL.
  Dependencies point inward. Each layer's Spring wiring lives in `reservation.a_config`, and Spring MVC registers the controller through
  `@RestController`.
- **Adapter-specific error handling.** The use case passes checked domain failures through to the input adapter. The HTTP controller returns a
  `409 Conflict` problem response for unavailable capacity. A future messaging adapter could catch the same exception and publish a rejection event.

### Trade-offs

- **Capacity by car type.** PostgreSQL starts with three cars per type. Reservations draw from that pool, and individual vehicle assignment is
  outside the exercise.
- **One lock per car type.** All requests for the same type serialize their availability checks and inserts, including requests for disjoint dates.
  Requests for different car types can proceed independently.
- **Time representation.** Offset-aware input is converted to an `Instant` so availability compares absolute times across time zones. Enforcing
  microsecond precision before booking prevents rounding during PostgreSQL comparisons and writes.
- **Explicit exception contracts.** Checked failures add `throws` declarations, an explicit `try`/`catch` around each adapter's use-case call,
  and a Spring rollback rule for checked exceptions.
- **Exercise scope.** Cancellation, authentication, price calculation, fleet administration are outside the exercise.
- **Zero logging.** Definitely not production ready, intended.
