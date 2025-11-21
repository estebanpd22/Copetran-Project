-- USERS
INSERT INTO users (full_name, email, phone, role, password_hash, created_at, status) VALUES
                                                                                         ('Juan Pérez García', 'juan.perez@example.com', '3001234567', 'DRIVER', '$2a$10$slYQmyNdGzin7olVN3p5be3DjH3zsizG4KLcWJ5.5X5.5X5.5X5', CURRENT_TIMESTAMP, 'ACTIVE'),
                                                                                         ('María López Rodríguez', 'maria.lopez@example.com', '3001234568', 'DRIVER', '$2a$10$slYQmyNdGzin7olVN3p5be3DjH3zsizG4KLcWJ5.5X5.5X5.5X5', CURRENT_TIMESTAMP, 'ACTIVE'),
                                                                                         ('Carlos Díaz Martínez', 'carlos.diaz@example.com', '3001234569', 'DISPATCHER', '$2a$10$slYQmyNdGzin7olVN3p5be3DjH3zsizG4KLcWJ5.5X5.5X5.5X5', CURRENT_TIMESTAMP, 'ACTIVE'),
                                                                                         ('Rosa Martínez Flores', 'rosa.martinez@example.com', '3001234572', 'ADMIN', '$2a$10$slYQmyNdGzin7olVN3p5be3DjH3zsizG4KLcWJ5.5X5.5X5.5X5', CURRENT_TIMESTAMP, 'ACTIVE'),
                                                                                         ('Francisco Ramírez López', 'francisco.ramirez@example.com', '3001234573', 'DRIVER', '$2a$10$slYQmyNdGzin7olVN3p5be3DjH3zsizG4KLcWJ5.5X5.5X5.5X5', CURRENT_TIMESTAMP, 'ACTIVE');

-- ROUTES
INSERT INTO routes (code, name, origin, destination, distance_km, duration_min) VALUES
                                                                                    ('RTA-001', 'Santa Marta - Barranquilla', 'Santa Marta', 'Barranquilla', 150.50, 240),
                                                                                    ('RTA-002', 'Santa Marta - Cartagena', 'Santa Marta', 'Cartagena', 200.00, 320);

-- BUSES
INSERT INTO buses (plate, capacity, bus_status, soat, amenities) VALUES
                                                                     ('ABC-123', 45, 'AVAILABLE', CURRENT_TIMESTAMP + INTERVAL '365 days', '[{"name":"WiFi"}]'),
                                                                     ('DEF-456', 45, 'AVAILABLE', CURRENT_TIMESTAMP + INTERVAL '365 days', '[{"name":"Aire Acondicionado"}]');

-- SEATS
INSERT INTO seats (number, type, status, price, bus_id) VALUES
                                                            (1, 'PREFERENTIAL', 'AVAILABLE', 85000.00, 1),
                                                            (2, 'PREFERENTIAL', 'AVAILABLE', 85000.00, 1),
                                                            (3, 'STANDARD', 'AVAILABLE', 65000.00, 1),
                                                            (4, 'STANDARD', 'AVAILABLE', 65000.00, 1),
                                                            (5, 'STANDARD', 'AVAILABLE', 65000.00, 1);

-- STOPS
INSERT INTO stops (name, stop_order, latitude, longitud, route_id) VALUES
                                                                       ('Avenida Principal Santa Marta', 1, 11.2408, -74.1990, 1),
                                                                       ('Centro Santa Marta', 2, 11.2430, -74.2050, 1),
                                                                       ('Salida Santa Marta', 3, 11.2500, -74.2100, 1),
                                                                       ('Ciénaga', 4, 11.0090, -74.2410, 1),
                                                                       ('Barranquilla Centro', 5, 10.9830, -74.8010, 1),
                                                                       ('Terminal Barranquilla', 6, 10.9630, -74.7960, 1);

INSERT INTO stops (name, stop_order, latitude, longitud, route_id) VALUES
                                                                       ('Salida Santa Marta', 1, 11.2500, -74.2100, 2),
                                                                       ('Ciénaga', 2, 11.0090, -74.2410, 2),
                                                                       ('Fundación', 3, 10.5180, -74.1850, 2),
                                                                       ('Cartagena Entrada', 4, 10.3990, -75.5140, 2),
                                                                       ('Cartagena Centro', 5, 10.4230, -75.5500, 2);

-- PASSENGERS
INSERT INTO passengers (full_name, document_type, document_number, phone_number, created_at, user_id) VALUES
                                                                                                          ('Ana Gómez', 'CC', '1001234567', '3001234570', CURRENT_TIMESTAMP, NULL),
                                                                                                          ('Luis Fernández', 'CC', '1001234568', '3001234571', CURRENT_TIMESTAMP, NULL),
                                                                                                          ('Paola Gutiérrez', 'CC', '1001234569', '3001234574', CURRENT_TIMESTAMP, NULL);

-- AMENITIES
INSERT INTO amenities (name) VALUES
                                 ('WiFi'),
                                 ('Aire Acondicionado'),
                                 ('Puertos USB'),
                                 ('Baño Químico');

-- FARE RULES
INSERT INTO fare_rules (base_price, dynamic_pricing, discounts, route_id)
VALUES
    (65000.00, 'OFF', '{"student": 0.15}'::jsonb, 1),
    (75000.00, 'ON', '{"elderly": 0.20}'::jsonb, 2);

-- TRIPS
INSERT INTO trips (date, departure_at, arrival_at, status, boarding_status, route_id, bus_id) VALUES
                                                                                                  (CURRENT_DATE, CURRENT_TIMESTAMP + INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '5 hours', 'SCHEDULED', 'NOT_STARTED', 1, 1),
                                                                                                  (CURRENT_DATE, CURRENT_TIMESTAMP + INTERVAL '3 hours', CURRENT_TIMESTAMP + INTERVAL '8 hours', 'SCHEDULED', 'NOT_STARTED', 2, 2);

-- TICKETS (corrigido a columnas reales)
INSERT INTO tickets (price, seat_number, ticket_status, passenger_id, trip_id)
VALUES
    (65000, '1', 'SOLD', 1, 1),
    (65000, '2', 'SOLD', 2, 1);

-- BAGGAGES
INSERT INTO baggages (weight_kg, fee, tag_code, ticket_id) VALUES
                                                               (15.5, 5000.00, 'BG001', 1),
                                                               (10.0, 3000.00, 'BG002', 2);

-- PARCELS
INSERT INTO parcels (code, sender_name, receiver_name, sender_phone, receiver_phone, price, createdAt, parcel_status, trip_id)
VALUES
    ('PRCL-001', 'Juan Pérez', 'Carlos Martínez', '3001111111', '3002222222', 15000, CURRENT_TIMESTAMP, 'IN_TRANSIT', 1),
    ('PRCL-002', 'Ana Gómez', 'Luis Soto', '3003333333', '3004444444', 25000, CURRENT_TIMESTAMP, 'CREATED', 2);

-- ASSIGNMENTS
INSERT INTO assignments (check_list_ok, assigned_at, trip_id, driver_id, dispatcher_id)
VALUES
    (true, CURRENT_TIMESTAMP, 1, 1, 3),
    (true, CURRENT_TIMESTAMP, 2, 2, 3);

-- CHECKLISTS
INSERT INTO checklists (trip_id, fuel_check, tire_check, brake_check, lights_check, emergency_equipment_check, documents_check, cleanliness_check, seats_check, completed)
VALUES
    (1, true, true, true, true, true, true, true, true, false),
    (2, true, true, true, true, true, true, true, true, false);

-- CASH CLOSES
INSERT INTO cash_closes (user_id, trip_id, expected_amount, actual_amount, cash_sales, card_sales, transfer_sales, closed_at, status)
VALUES
    (3, 1, 150000, 150000, 50000, 80000, 20000, CURRENT_TIMESTAMP, 'CLOSED');

-- CONFIG
INSERT INTO config (key, value) VALUES
                                    ('SEAT_HOLD_TIMEOUT_MINUTES', '30'),
                                    ('MAX_LUGGAGE_WEIGHT_KG', '30'),
                                    ('SYSTEM_EMAIL', 'noreply@copetran.com');
