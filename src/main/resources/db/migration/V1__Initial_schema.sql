
CREATE TYPE user_role AS ENUM ('PASSENGER','ADMIN','CLERK','DRIVER','DISPATCHER');
CREATE TYPE user_status AS ENUM ('ACTIVE','INACTIVE','BLOCKED');
CREATE TYPE bus_status AS ENUM ('AVAILABLE','ASSIGNED','IN_SERVICE','IN_MAINTENANCE','OUT_OF_SERVICE');
CREATE TYPE boarding_status AS ENUM ('NOT_STARTED','OPEN','CLOSED');
CREATE TYPE seat_status AS ENUM ('AVAILABLE','UNAVAILABLE');
CREATE TYPE seat_type AS ENUM ('STANDARD','PREFERENTIAL');
CREATE TYPE seat_hold_status AS ENUM ('HOLD','EXPIRED');
CREATE TYPE payment_method AS ENUM ('CASH','TRANSFER','QR','CARD');
CREATE TYPE payment_status AS ENUM ('PENDING','CONFIRMED','CANCELLED');
CREATE TYPE ticket_status AS ENUM ('SOLD','CANCELLED','NO_SHOW');
CREATE TYPE trip_status AS ENUM ('SCHEDULED','BOARDING','DEPARTED','ARRIVED','CANCELLED');
CREATE TYPE cash_close_status AS ENUM ('OPEN','CLOSED','RECONCILED','DISCREPANCY');
CREATE TYPE dynamic_pricing AS ENUM ('ON','OFF');
CREATE TYPE entity_type AS ENUM ('TRIP','TICKET','PARCEL');
CREATE TYPE incident_type AS ENUM ('SECURITY','DELIVERY_FAIL','OVERBOOK','VEHICLE');
CREATE TYPE parcel_status AS ENUM ('CREATED','IN_TRANSIT','DELIVERED','FAILED');

CREATE CAST (varchar AS user_role) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS user_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS bus_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS boarding_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS seat_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS seat_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS seat_hold_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS payment_method) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS payment_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS ticket_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS trip_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS cash_close_status) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS dynamic_pricing) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS entity_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS incident_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS parcel_status) WITH INOUT AS IMPLICIT;


-- ========== TABLES INDEPENDENT FIRST ==========
CREATE TABLE amenities (
                           amenity_id BIGSERIAL PRIMARY KEY,
                           name VARCHAR NOT NULL
);

CREATE TABLE config (
                        config_id BIGSERIAL PRIMARY KEY,
                        key VARCHAR NOT NULL UNIQUE,
                        value VARCHAR NOT NULL
);

CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR NOT NULL,
                       email VARCHAR NOT NULL UNIQUE,
                       phone VARCHAR NOT NULL,
                       role user_role NOT NULL,
                       password_hash VARCHAR NOT NULL,
                       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                       status user_status NOT NULL
);

CREATE TABLE buses (
                       bus_id BIGSERIAL PRIMARY KEY,
                       plate VARCHAR NOT NULL UNIQUE,
                       capacity INTEGER NOT NULL,
                       bus_status bus_status NOT NULL,
                       soat TIMESTAMPTZ NOT NULL,
                       amenities JSONB DEFAULT '[]'::jsonb
);

CREATE TABLE routes (
                        route_id BIGSERIAL PRIMARY KEY,
                        code VARCHAR NOT NULL,
                        name VARCHAR NOT NULL,
                        origin VARCHAR NOT NULL,
                        destination VARCHAR NOT NULL,
                        distance_km REAL NOT NULL,
                        duration_min REAL NOT NULL
);

CREATE TABLE stops (
                       stop_id BIGSERIAL PRIMARY KEY,
                       name VARCHAR NOT NULL,
                       stop_order INTEGER NOT NULL,
                       latitude DOUBLE PRECISION NOT NULL,
                       longitud DOUBLE PRECISION NOT NULL,
                       route_id BIGINT REFERENCES routes(route_id) ON DELETE SET NULL
);

-- ========== FARE RULES (depends on routes & stops) ==========
CREATE TABLE fare_rules (
                             fare_rule_id BIGSERIAL PRIMARY KEY,
                             base_price NUMERIC NOT NULL,
                             dynamic_pricing dynamic_pricing NOT NULL,
                             discounts JSONB,
                             route_id BIGINT REFERENCES routes(route_id) ON DELETE SET NULL,
                             from_stop_id BIGINT REFERENCES stops(stop_id) ON DELETE SET NULL,
                             to_stop_id BIGINT REFERENCES stops(stop_id) ON DELETE SET NULL
);

-- ========== TRIPS, CHECKLIST, ASSIGNMENT, CASH CLOSE ==========
CREATE TABLE trips (
                       trip_id BIGSERIAL PRIMARY KEY,
                       date DATE NOT NULL,
                       departure_at TIMESTAMPTZ NOT NULL,
                       arrival_at TIMESTAMPTZ NOT NULL,
                       status trip_status NOT NULL,
                       boarding_status boarding_status NOT NULL DEFAULT 'NOT_STARTED',
                       actual_departure_at TIMESTAMPTZ,
                       route_id BIGINT REFERENCES routes(route_id) ON DELETE SET NULL,
                       bus_id BIGINT REFERENCES buses(bus_id) ON DELETE SET NULL
);

CREATE TABLE checklists (
                            checklist_id BIGSERIAL PRIMARY KEY,
                            trip_id BIGINT NOT NULL UNIQUE,
                            fuel_check BOOLEAN NOT NULL DEFAULT FALSE,
                            tire_check BOOLEAN NOT NULL DEFAULT FALSE,
                            brake_check BOOLEAN NOT NULL DEFAULT FALSE,
                            lights_check BOOLEAN NOT NULL DEFAULT FALSE,
                            emergency_equipment_check BOOLEAN NOT NULL DEFAULT FALSE,
                            documents_check BOOLEAN NOT NULL DEFAULT FALSE,
                            cleanliness_check BOOLEAN NOT NULL DEFAULT FALSE,
                            seats_check BOOLEAN NOT NULL DEFAULT FALSE,
                            completed BOOLEAN NOT NULL DEFAULT FALSE,
                            completed_by_user_id BIGINT,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            completed_at TIMESTAMPTZ,
                            notes VARCHAR(500),
                            CONSTRAINT fk_checklist_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
                            CONSTRAINT fk_checklist_user FOREIGN KEY (completed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE assignments (
                             assinment_id BIGSERIAL PRIMARY KEY,
                             check_list_ok BOOLEAN NOT NULL,
                             assigned_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                             trip_id BIGINT UNIQUE,
                             driver_id BIGINT,
                             dispatcher_id BIGINT,
                             CONSTRAINT fk_assignment_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE SET NULL,
                             CONSTRAINT fk_assignment_driver FOREIGN KEY (driver_id) REFERENCES users(user_id) ON DELETE SET NULL,
                             CONSTRAINT fk_assignment_dispatcher FOREIGN KEY (dispatcher_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE cash_closes (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT,
                             trip_id BIGINT,
                             expected_amount NUMERIC,
                             actual_amount NUMERIC,
                             cash_sales NUMERIC,
                             card_sales NUMERIC,
                             transfer_sales NUMERIC,
                             closed_at TIMESTAMPTZ,
                             status cash_close_status,
                             CONSTRAINT fk_cashclose_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
                             CONSTRAINT fk_cashclose_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE SET NULL
);

-- ========== SEATS, SEAT HOLDS ==========
CREATE TABLE seats (
                       seat_id BIGSERIAL PRIMARY KEY,
                       price NUMERIC NOT NULL,
                       number INTEGER NOT NULL,
                       type seat_type NOT NULL,
                       status seat_status NOT NULL,
                       bus_id BIGINT,
                       CONSTRAINT fk_seat_bus FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE
);

CREATE TABLE "seatHolds" (
                             seat_hold_id BIGSERIAL PRIMARY KEY,
                             seat_number VARCHAR NOT NULL,
                             expire_at TIMESTAMPTZ NOT NULL,
                             seat_hold_status seat_hold_status NOT NULL,
                             seat_id BIGINT UNIQUE,
                             trip_id BIGINT,
                             user_id BIGINT,
                             CONSTRAINT fk_seathold_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE SET NULL,
                             CONSTRAINT fk_seathold_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
                             CONSTRAINT fk_seathold_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ========== PASSENGERS, USERS relations ==========
CREATE TABLE passengers (
                            id BIGSERIAL PRIMARY KEY,
                            full_name VARCHAR NOT NULL,
                            document_type VARCHAR,
                            document_number VARCHAR,
                            birth_date DATE,
                            phone_number VARCHAR,
                            created_at TIMESTAMPTZ,
                            user_id BIGINT,
                            CONSTRAINT fk_passenger_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ========== PURCHASES, PAYMENTS, TICKETS ==========
CREATE TABLE purchases (
                           purchase_id BIGSERIAL PRIMARY KEY,
                           payment_method payment_method NOT NULL,
                           total_amount NUMERIC NOT NULL,
                           payment_status payment_status NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL,
                           user_id BIGINT,
                           CONSTRAINT fk_purchase_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE payments (
                          payment_id BIGSERIAL PRIMARY KEY,
                          purchase_id BIGINT,
                          payment_method payment_method NOT NULL,
                          payment_status payment_status NOT NULL,
                          amount NUMERIC NOT NULL,
                          payment_reference VARCHAR NOT NULL,
                          confirmation_code VARCHAR NOT NULL,
                          confirmed_at TIMESTAMPTZ NOT NULL,
                          CONSTRAINT fk_payment_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id) ON DELETE SET NULL
);

CREATE TABLE tickets (
                         ticket_id BIGSERIAL PRIMARY KEY,
                         price NUMERIC NOT NULL,
                         seat_number VARCHAR NOT NULL,
                         ticket_status ticket_status NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         qr_code VARCHAR,
                         purchase_id BIGINT,
                         passenger_id BIGINT NOT NULL,
                         trip_id BIGINT,
                         seat_id BIGINT,
                         from_stop_id BIGINT,
                         to_stop_id BIGINT,
                         CONSTRAINT fk_ticket_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id) ON DELETE SET NULL,
                         CONSTRAINT fk_ticket_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE,
                         CONSTRAINT fk_ticket_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE SET NULL,
                         CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE SET NULL,
                         CONSTRAINT fk_ticket_from_stop FOREIGN KEY (from_stop_id) REFERENCES stops(stop_id) ON DELETE SET NULL,
                         CONSTRAINT fk_ticket_to_stop FOREIGN KEY (to_stop_id) REFERENCES stops(stop_id) ON DELETE SET NULL
);

CREATE TABLE baggages (
                          baggage_id BIGSERIAL PRIMARY KEY,
                          weight_kg REAL NOT NULL,
                          fee NUMERIC NOT NULL,
                          tag_code VARCHAR,
                          ticket_id BIGINT,
                          CONSTRAINT fk_baggage_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE SET NULL
);

CREATE TABLE parcels (
                         parcel_id BIGSERIAL PRIMARY KEY,
                         code VARCHAR NOT NULL UNIQUE,
                         sender_name VARCHAR(120) NOT NULL,
                         receiver_name VARCHAR(120) NOT NULL,
                         sender_phone VARCHAR(10) NOT NULL,
                         receiver_phone VARCHAR(10) NOT NULL,
                         price NUMERIC NOT NULL,
                         createdAt TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                         parcel_status parcel_status NOT NULL,
                         proof_photo_url VARCHAR,
                         delivery_otp VARCHAR,
                         trip_id BIGINT,
                         from_stop_id BIGINT,
                         to_stop_id BIGINT,
                         CONSTRAINT fk_parcel_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE SET NULL,
                         CONSTRAINT fk_parcel_from_stop FOREIGN KEY (from_stop_id) REFERENCES stops(stop_id) ON DELETE SET NULL,
                         CONSTRAINT fk_parcel_to_stop FOREIGN KEY (to_stop_id) REFERENCES stops(stop_id) ON DELETE SET NULL
);

-- ========== INCIDENTS ==========
CREATE TABLE incidents (
                           incident_id BIGSERIAL PRIMARY KEY,
                           entity_type entity_type NOT NULL,
                           entity_id BIGINT NOT NULL,
                           incident_type incident_type NOT NULL,
                           note VARCHAR NOT NULL,
                           created_at TIME WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_trips_route_date ON trips(route_id, date);
CREATE INDEX idx_tickets_passenger ON tickets(passenger_id);
CREATE INDEX idx_purchases_user ON purchases(user_id);
CREATE INDEX idx_seats_bus_number ON seats(bus_id, number);
CREATE INDEX idx_seatholds_user_trip ON "seatHolds"(user_id, trip_id);
