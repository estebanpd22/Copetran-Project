-- V1__create_core_tables.sql
SET search_path = public;

-- ========== TABLA AMENITIES ==========
CREATE TABLE IF NOT EXISTS amenities (
                                         amenity_id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL
    );

-- ========== TABLA USUARIOS ==========
CREATE TABLE IF NOT EXISTS users (
                                     user_id BIGSERIAL PRIMARY KEY,
                                     full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('PASSENGER', 'ADMIN', 'CLERK', 'DRIVER', 'DISPATCHER')),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ========== TABLA PASAJEROS ==========
CREATE TABLE IF NOT EXISTS passengers (
                                          id BIGSERIAL PRIMARY KEY,
                                          full_name VARCHAR(200) NOT NULL,
    document_type VARCHAR(50),
    document_number VARCHAR(100),
    birth_date DATE,
    phone_number VARCHAR(30),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    user_id BIGINT,
    CONSTRAINT fk_passenger_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
    );

CREATE INDEX IF NOT EXISTS idx_passengers_user ON passengers(user_id);
CREATE INDEX IF NOT EXISTS idx_passengers_document ON passengers(document_type, document_number);

-- ========== TABLA RUTAS ==========
CREATE TABLE IF NOT EXISTS routes (
                                      route_id BIGSERIAL PRIMARY KEY,
                                      code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    origin VARCHAR(150) NOT NULL,
    destination VARCHAR(150) NOT NULL,
    distance_km REAL NOT NULL,
    duration_min REAL NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_routes_code ON routes(code);
CREATE INDEX IF NOT EXISTS idx_routes_origin_dest ON routes(origin, destination);

-- ========== TABLA PARADAS ==========
CREATE TABLE IF NOT EXISTS stops (
                                     stop_id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(150) NOT NULL,
    stop_order INTEGER NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    route_id BIGINT NOT NULL,
    CONSTRAINT fk_stop_route FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_stops_route ON stops(route_id);
CREATE INDEX IF NOT EXISTS idx_stops_order ON stops(route_id, stop_order);

-- ========== TABLA BUSES ==========
CREATE TABLE IF NOT EXISTS buses (
                                     bus_id BIGSERIAL PRIMARY KEY,
                                     plate VARCHAR(50) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    bus_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' CHECK (bus_status IN ('AVAILABLE', 'ASSIGNED', 'IN_SERVICE', 'IN_MAINTENANCE', 'OUT_OF_SERVICE')),
    soat TIMESTAMPTZ NOT NULL,
    amenities JSONB DEFAULT '[]'
    );

CREATE INDEX IF NOT EXISTS idx_buses_plate ON buses(plate);
CREATE INDEX IF NOT EXISTS idx_buses_status ON buses(bus_status);

-- ========== TABLA ASIENTOS ==========
CREATE TABLE IF NOT EXISTS seats (
                                     seat_id BIGSERIAL PRIMARY KEY,
                                     price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    number INTEGER NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'STANDARD' CHECK (type IN ('STANDARD', 'PREFERENTIAL')),
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'UNAVAILABLE')),
    bus_id BIGINT NOT NULL,
    CONSTRAINT fk_seat_bus FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_seat_bus_number ON seats(bus_id, number);
CREATE INDEX IF NOT EXISTS idx_seats_bus ON seats(bus_id);
CREATE INDEX IF NOT EXISTS idx_seats_status ON seats(status);

-- ========== TABLA VIAJES ==========
CREATE TABLE IF NOT EXISTS trips (
                                     trip_id BIGSERIAL PRIMARY KEY,
                                     date DATE NOT NULL,
                                     departure_at TIMESTAMPTZ NOT NULL,
                                     arrival_at TIMESTAMPTZ NOT NULL,
                                     status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'BOARDING', 'DEPARTED', 'ARRIVED', 'CANCELLED')),
    route_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE RESTRICT,
    CONSTRAINT fk_trip_bus FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_trips_route ON trips(route_id);
CREATE INDEX IF NOT EXISTS idx_trips_bus ON trips(bus_id);
CREATE INDEX IF NOT EXISTS idx_trips_date ON trips(date);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);

-- ========== TABLA REGLAS TARIFARIAS ==========
CREATE TABLE IF NOT EXISTS fareRules (
                                         fare_rule_id BIGSERIAL PRIMARY KEY,
                                         base_price NUMERIC(12,2) NOT NULL CHECK (base_price >= 0),
    dynamic_pricing VARCHAR(10) NOT NULL DEFAULT 'OFF' CHECK (dynamic_pricing IN ('ON', 'OFF')),
    discounts JSONB DEFAULT '{}',
    route_id BIGINT NOT NULL,
    from_stop_id BIGINT NOT NULL,
    to_stop_id BIGINT NOT NULL,
    CONSTRAINT fk_farerule_route FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE,
    CONSTRAINT fk_farerule_fromstop FOREIGN KEY (from_stop_id) REFERENCES stops(stop_id) ON DELETE RESTRICT,
    CONSTRAINT fk_farerule_tostop FOREIGN KEY (to_stop_id) REFERENCES stops(stop_id) ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_farerules_route ON fareRules(route_id);
CREATE INDEX IF NOT EXISTS idx_farerules_stops ON fareRules(from_stop_id, to_stop_id);

-- ========== TABLA COMPRAS ==========
CREATE TABLE IF NOT EXISTS purchases (
                                         purchase_id BIGSERIAL PRIMARY KEY,
                                         payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CASH', 'TRANSFER', 'QR', 'CARD')),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_purchase_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_purchases_user ON purchases(user_id);
CREATE INDEX IF NOT EXISTS idx_purchases_status ON purchases(payment_status);

-- ========== TABLA TICKETS ==========
CREATE TABLE IF NOT EXISTS tickets (
                                       ticket_id BIGSERIAL PRIMARY KEY,
                                       price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    seat_number VARCHAR(30) NOT NULL,
    ticket_status VARCHAR(20) NOT NULL DEFAULT 'SOLD' CHECK (ticket_status IN ('SOLD', 'CANCELLED', 'NO_SHOW')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    qr_code TEXT,
    purchase_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    seat_id BIGINT,
    from_stop_id BIGINT NOT NULL,
    to_stop_id BIGINT NOT NULL,
    CONSTRAINT fk_ticket_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_fromstop FOREIGN KEY (from_stop_id) REFERENCES stops(stop_id) ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_tostop FOREIGN KEY (to_stop_id) REFERENCES stops(stop_id) ON DELETE RESTRICT
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_ticket_trip_seat ON tickets(trip_id, seat_number);
CREATE INDEX IF NOT EXISTS idx_tickets_purchase ON tickets(purchase_id);
CREATE INDEX IF NOT EXISTS idx_tickets_passenger ON tickets(passenger_id);
CREATE INDEX IF NOT EXISTS idx_tickets_trip ON tickets(trip_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(ticket_status);

-- ========== TABLA EQUIPAJE ==========
CREATE TABLE IF NOT EXISTS baggages (
                                        baggage_id BIGSERIAL PRIMARY KEY,
                                        weight_kg REAL NOT NULL CHECK (weight_kg >= 0),
    fee NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (fee >= 0),
    tag_code VARCHAR(100),
    ticket_id BIGINT NOT NULL,
    CONSTRAINT fk_baggage_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_baggage_tag ON baggages(tag_code) WHERE tag_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_baggages_ticket ON baggages(ticket_id);

-- ========== TABLA ENCOMIENDAS ==========
CREATE TABLE IF NOT EXISTS parcels (
                                       parcel_id BIGSERIAL PRIMARY KEY,
                                       code VARCHAR(100) NOT NULL UNIQUE,
    sender_name VARCHAR(120) NOT NULL,
    receiver_name VARCHAR(120) NOT NULL,
    sender_phone VARCHAR(10) NOT NULL,
    receiver_phone VARCHAR(10) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    parcel_status VARCHAR(20) NOT NULL DEFAULT 'CREATED' CHECK (parcel_status IN ('CREATED', 'IN_TRANSIT', 'DELIVERED', 'FAILED')),
    proof_photo_url TEXT,
    delivery_otp VARCHAR(20),
    trip_id BIGINT,
    from_stop_id BIGINT NOT NULL,
    to_stop_id BIGINT NOT NULL,
    CONSTRAINT fk_parcel_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE SET NULL,
    CONSTRAINT fk_parcel_fromstop FOREIGN KEY (from_stop_id) REFERENCES stops(stop_id) ON DELETE RESTRICT,
    CONSTRAINT fk_parcel_tostop FOREIGN KEY (to_stop_id) REFERENCES stops(stop_id) ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_parcels_trip ON parcels(trip_id);
CREATE INDEX IF NOT EXISTS idx_parcels_status ON parcels(parcel_status);
CREATE INDEX IF NOT EXISTS idx_parcels_code ON parcels(code);

-- ========== TABLA ASIGNACIONES ==========
CREATE TABLE IF NOT EXISTS assignments (
                                           assinment_id BIGSERIAL PRIMARY KEY,
                                           check_list_ok BOOLEAN NOT NULL DEFAULT FALSE,
                                           assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           trip_id BIGINT NOT NULL UNIQUE,
                                           driver_id BIGINT NOT NULL,
                                           dispatcher_id BIGINT NOT NULL,
                                           CONSTRAINT fk_assignment_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_driver FOREIGN KEY (driver_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_assignment_dispatcher FOREIGN KEY (dispatcher_id) REFERENCES users(user_id) ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_assignments_driver ON assignments(driver_id);
CREATE INDEX IF NOT EXISTS idx_assignments_dispatcher ON assignments(dispatcher_id);

-- ========== TABLA RESERVAS DE ASIENTOS ==========
CREATE TABLE IF NOT EXISTS seatHolds (
                                         seat_hold_id BIGSERIAL PRIMARY KEY,
                                         seat_number VARCHAR(30) NOT NULL,
    expire_at TIMESTAMPTZ NOT NULL,
    seat_hold_status VARCHAR(20) NOT NULL DEFAULT 'HOLD' CHECK (seat_hold_status IN ('HOLD', 'EXPIRED')),
    seat_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_seathold_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE,
    CONSTRAINT fk_seathold_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_seathold_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_seathold_trip_seat ON seatHolds(trip_id, seat_number) WHERE seat_hold_status = 'HOLD';
CREATE INDEX IF NOT EXISTS idx_seatholds_trip ON seatHolds(trip_id);
CREATE INDEX IF NOT EXISTS idx_seatholds_user ON seatHolds(user_id);
CREATE INDEX IF NOT EXISTS idx_seatholds_expire ON seatHolds(expire_at) WHERE seat_hold_status = 'HOLD';

-- ========== TABLA INCIDENTES ==========
CREATE TABLE IF NOT EXISTS incidents (
                                         incident_id BIGSERIAL PRIMARY KEY,
                                         entity_type VARCHAR(20) NOT NULL CHECK (entity_type IN ('TRIP', 'TICKET', 'PARCEL')),
    entity_id BIGINT NOT NULL,
    incident_type VARCHAR(20) NOT NULL CHECK (incident_type IN ('SECURITY', 'DELIVERY_FAIL', 'OVERBOOK', 'VEHICLE')),
    note TEXT NOT NULL,
    created_at TIME NOT NULL DEFAULT CURRENT_TIME
    );

CREATE INDEX IF NOT EXISTS idx_incidents_entity ON incidents(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_incidents_type ON incidents(incident_type);

-- ========== TABLA CONFIGURACIÓN ==========
CREATE TABLE IF NOT EXISTS config (
                                      config_id BIGSERIAL PRIMARY KEY,
                                      key VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_config_key ON config(key);

COMMIT;