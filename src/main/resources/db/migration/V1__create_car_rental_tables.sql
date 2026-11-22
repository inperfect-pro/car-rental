CREATE TABLE car_fleet (
    car_type VARCHAR(10) PRIMARY KEY,
    capacity INTEGER NOT NULL,
    CONSTRAINT car_fleet_car_type_check CHECK (car_type IN ('SEDAN', 'SUV', 'VAN')),
    CONSTRAINT car_fleet_capacity_check CHECK (capacity > 0)
);

CREATE TABLE car_reservation (
    reservation_id VARCHAR(36) PRIMARY KEY,
    car_type VARCHAR(10) NOT NULL REFERENCES car_fleet (car_type),
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT car_reservation_period_check CHECK (ends_at > starts_at)
);

CREATE INDEX car_reservation_availability_index
    ON car_reservation (car_type, starts_at, ends_at);

INSERT INTO car_fleet (car_type, capacity)
VALUES ('SEDAN', 3), ('SUV', 3), ('VAN', 3);
