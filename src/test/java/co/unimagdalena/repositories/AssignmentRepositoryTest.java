package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AssignmentRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    SeatRepository seatRepository;

    private Seat createSeat(Integer number, BigDecimal price,
                            SeatType type, SeatStatus status) {
        return seatRepository.save(Seat.builder().
                number(number)
                .price(price)
                .type(type)
                .status(status).build());
    }

    private Route createRoute(String code, String origin, String destination,
                              Float distanceKm, Float durationMin, String name) {
        return routeRepository.save(Route.builder().code(code)
                .name(name).origin(origin).destination(destination)
                .distanceKm(distanceKm).durationMin(durationMin).build());
    }

    private User createUser(String fullName, String email,
                            String password, UserRole role,
                            UserStatus status, LocalDateTime date, String phone) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(password)
                .role(role).status(status)
                .phone(phone)
                .createdAt(date).build());
    }

    private Assignment createAssignment(LocalDateTime assignedAt, boolean checkListOk,
                                        Trip trip, User dispatcher, User driver) {
        return assignmentRepository.save(Assignment.builder().
                assignedAt(assignedAt)
                .checkListOk(checkListOk)
                .trip(trip)
                .dispatcher(dispatcher)
                .driver(driver).build());
    }

    private Trip createTrip(LocalDate date, OffsetDateTime departureAt,
                            OffsetDateTime arrivalAt, TripStatus status,
                            Route route) {
        return tripRepository.save(Trip.builder()
                .date(date)
                .departureAt(departureAt)
                .arrivalAt(arrivalAt)
                .status(status)
                .route(route).build());
    }

    private Bus createBusWithOutSaving(String plate, Set<Amenity> amenities,
                                       Integer capacity, OffsetDateTime soatExpirationDate,
                                       BusStatus status) {
        return Bus.builder().
                plate(plate)
                .amenities(amenities)
                .capacity(capacity)
                .soatExpirationDate(soatExpirationDate)
                .status(status).build();
    }

    ;

    @Test
    @DisplayName("Debe encontrar la asignacion por el id del viaje")
    void shouldFindByTripId() {
        //Given
        LocalDateTime date = LocalDateTime.now();
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(2);
        LocalDate date1 = LocalDate.now();

        Route route = createRoute("code", "Barranquilla",
                "Bucaramanga", Float.MIN_NORMAL,
                Float.MIN_NORMAL, "barranquillaBucaramanga");

        Bus bus = createBusWithOutSaving("KS21",
                Set.of(new Amenity(1L, "dos pisos")),
                2,
                OffsetDateTime.now().plusMonths(2),
                BusStatus.ASSIGNED);

        Seat seat1 = createSeat(3, BigDecimal.valueOf(25000), SeatType.STANDARD, SeatStatus.TAKEN);
        Seat seat2 = createSeat(4, BigDecimal.valueOf(25000), SeatType.STANDARD, SeatStatus.ON_HOLD);

        bus.setSeats(List.of(seat1, seat2));

        Trip trip1 = createTrip(date1.plusDays(2), departure, arrival, TripStatus.BOARDING, route);

        bus.addTrip(trip1);
        bus = busRepository.save(bus);

        User driver = createUser("Carlitos driver", "carlitosdriver@gmail.com"
                , "driver123", UserRole.DRIVER, UserStatus.ACTIVE, date.minusDays(3), "3044978652");

        User dispatcher = createUser("Carlitos dispatcher", "carlitosdispatcher@gmail.com"
                , "dispatcher123", UserRole.DISPATCHER, UserStatus.ACTIVE, date.minusDays(2), "3044978652");

        Assignment assignment = createAssignment(date, true, trip1, dispatcher, driver);

        //When
        Optional<Assignment> found = assignmentRepository.findAssignmentByTrip_Id(trip1.getId());

        //Then
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().isCheckListOk()).isTrue();
        assertThat(found.get().getAssignedAt()).isEqualTo(date);

        assertThat(found.get().getTrip().getId()).isNotNull();
        assertThat(found.get().getTrip().getDate()).isEqualTo(date1.plusDays(2));
        assertThat(found.get().getTrip().getDepartureAt()).isEqualTo(departure);
        assertThat(found.get().getTrip().getArrivalAt()).isEqualTo(arrival);
        assertThat(found.get().getTrip().getStatus()).isEqualTo(TripStatus.BOARDING);
        assertThat(found.get().getTrip().getRoute().getId()).isNotNull();

        assertThat(found.get().getDriver().getId()).isNotNull();
        assertThat(found.get().getDriver().getCreatedAt()).isEqualTo(date.minusDays(3));
        assertThat(found.get().getDriver().getFullName()).isEqualTo("Carlitos driver");
        assertThat(found.get().getDriver().getEmail()).isEqualTo("carlitosdriver@gmail.com");
        assertThat(found.get().getDriver().getPasswordHash()).isEqualTo("driver123");
        assertThat(found.get().getDriver().getPhone()).isEqualTo("3044978652");
        assertThat(found.get().getDriver().getRole()).isEqualTo(UserRole.DRIVER);
        assertThat(found.get().getDriver().getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(found.get().getDispatcher().getId()).isNotNull();
        assertThat(found.get().getDispatcher().getCreatedAt()).isEqualTo(date.minusDays(2));
        assertThat(found.get().getDispatcher().getFullName()).isEqualTo("Carlitos dispatcher");
        assertThat(found.get().getDispatcher().getEmail()).isEqualTo("carlitosdispatcher@gmail.com");
        assertThat(found.get().getDispatcher().getPasswordHash()).isEqualTo("dispatcher123");
        assertThat(found.get().getDispatcher().getPhone()).isEqualTo("3044978652");
        assertThat(found.get().getDispatcher().getRole()).isEqualTo(UserRole.DISPATCHER);
        assertThat(found.get().getDispatcher().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe encontrar las asignaciones segun el id del driver")
    void shouldFindByDriver(){
        //Given
        LocalDateTime date = LocalDateTime.now().minusMonths(2);
        LocalDateTime actualTime = LocalDateTime.now();
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(2);
        LocalDate date1 = LocalDate.now();

        Route route = createRoute("code", "Barranquilla",
                "Bucaramanga", Float.MIN_NORMAL,
                Float.MIN_NORMAL, "barranquillaBucaramanga");

        Bus bus = createBusWithOutSaving("KS21",
                Set.of(new Amenity(1L, "dos pisos")),
                2,
                OffsetDateTime.now().plusMonths(2),
                BusStatus.ASSIGNED);

        Seat seat1 = createSeat(3, BigDecimal.valueOf(25000), SeatType.STANDARD, SeatStatus.TAKEN);
        Seat seat2 = createSeat(4, BigDecimal.valueOf(25000), SeatType.STANDARD, SeatStatus.ON_HOLD);

        bus.setSeats(List.of(seat1, seat2));

        Trip trip1 = createTrip(date1.plusDays(2), departure, arrival, TripStatus.BOARDING, route);

        bus.addTrip(trip1);
        bus = busRepository.save(bus);

        User driver = createUser("Puellito driver", "puellitodriver@gmail.com",
                "driverpassword123", UserRole.DRIVER,
                UserStatus.ACTIVE, date, "3002389429");
        User dispatcher = createUser("Puellito dispatcher", "puellitodispatcher@gmail.com",
                "dispatcher123", UserRole.DISPATCHER, UserStatus.ACTIVE,
                actualTime.minusDays(2), "3044978652");

        Assignment assignment = createAssignment(actualTime, true, trip1, dispatcher, driver);


        //When
        List<Assignment> assignments = assignmentRepository.findByDriver(driver.getId());

        //Then
        assertThat(assignments.size()).isEqualTo(1);
        assertThat(assignments.getFirst()).isNotNull();

        assertThat(assignments.getFirst().getDriver().getId()).isNotNull();
        assertThat(assignments.getFirst().getDriver().getFullName()).isEqualTo("Puellito driver");
        assertThat(assignments.getFirst().getDriver().getEmail()).isEqualTo("puellitodriver@gmail.com");
        assertThat(assignments.getFirst().getDriver().getPasswordHash()).isEqualTo("driverpassword123");
        assertThat(assignments.getFirst().getDriver().getRole()).isEqualTo(UserRole.DRIVER);
        assertThat(assignments.getFirst().getDriver().getCreatedAt()).isEqualTo(date);
        assertThat(assignments.getFirst().getDriver().getPhone()).isEqualTo("3002389429");
        assertThat(assignments.getFirst().getDriver().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe encontrar las asignaciones segun el id del dispatcher")
    void shouldFindByDispatcher(){
        //Given
        LocalDateTime date = LocalDateTime.now().minusMonths(2);
        LocalDateTime actualTime = LocalDateTime.now();
        OffsetDateTime departure = OffsetDateTime.now();
        OffsetDateTime arrival = OffsetDateTime.now().plusHours(2);
        LocalDate date1 = LocalDate.now();

        Route route = createRoute("code", "Barranquilla",
                "Bucaramanga", Float.MIN_NORMAL,
                Float.MIN_NORMAL, "barranquillaBucaramanga");

        Bus bus = createBusWithOutSaving("KS21",
                Set.of(new Amenity(1L, "dos pisos")),
                2,
                OffsetDateTime.now().plusMonths(2),
                BusStatus.ASSIGNED);

        Seat seat1 = createSeat(3, BigDecimal.valueOf(25000), SeatType.STANDARD, SeatStatus.TAKEN);
        Seat seat2 = createSeat(4, BigDecimal.valueOf(25000), SeatType.STANDARD, SeatStatus.ON_HOLD);

        bus.setSeats(List.of(seat1, seat2));

        Trip trip1 = createTrip(date1.plusDays(2), departure, arrival, TripStatus.BOARDING, route);

        bus.addTrip(trip1);
        bus = busRepository.save(bus);

        User driver = createUser("Puellito driver", "puellitodriver@gmail.com",
                "driverpassword123", UserRole.DRIVER,
                UserStatus.ACTIVE, date, "3002389429");
        User dispatcher = createUser("Puellito dispatcher", "puellitodispatcher@gmail.com",
                "dispatcher123", UserRole.DISPATCHER, UserStatus.ACTIVE,
                actualTime.minusDays(2), "3044978652");

        Assignment assignment = createAssignment(actualTime, true, trip1, dispatcher, driver);


        //When
        List<Assignment> assignments = assignmentRepository.findByDispatcher(dispatcher.getId());

        //Then
        assertThat(assignments.size()).isEqualTo(1);
        assertThat(assignments.getFirst()).isNotNull();

        assertThat(assignments.getFirst().getDispatcher().getId()).isNotNull();
        assertThat(assignments.getFirst().getDispatcher().getFullName()).isEqualTo("Puellito dispatcher");
        assertThat(assignments.getFirst().getDispatcher().getEmail()).isEqualTo("puellitodispatcher@gmail.com");
        assertThat(assignments.getFirst().getDispatcher().getPasswordHash()).isEqualTo("dispatcher123");
        assertThat(assignments.getFirst().getDispatcher().getRole()).isEqualTo(UserRole.DISPATCHER);
        assertThat(assignments.getFirst().getDispatcher().getCreatedAt()).isEqualTo(actualTime.minusDays(2));
        assertThat(assignments.getFirst().getDispatcher().getPhone()).isEqualTo("3044978652");
        assertThat(assignments.getFirst().getDispatcher().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}

