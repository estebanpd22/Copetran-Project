package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.Passenger;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.PassengerRepository;
import co.unimagdalena.domine.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PassengerRepositoryTest extends AbstractRepositoryTI {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        passengerRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -------- Helpers --------

    private User givenUser() {
        return givenUser("user@test.com", "Juan Pérez", UserRole.PASSENGER);
    }

    private User givenUser(String email, String fullName, UserRole role) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .phone("3001234567")
                .passwordHash("hashedPassword")
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    private Passenger buildPassenger(String name, String doc, User user) {
        return Passenger.builder()
                .fullName(name)
                .documentType("CC")
                .documentNumber(doc)
                .birthDate(LocalDate.of(1995, 5, 5))
                .phoneNumber("3009876543")
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();
    }

    private Passenger givenPassenger() {
        return passengerRepository.save(buildPassenger("Pedro Gómez", "123456789", null));
    }

    private Passenger givenPassenger(User user, String documentNumber) {
        Passenger passenger = buildPassenger("Maria López", documentNumber, user);
        return passengerRepository.save(passenger);
    }

    // -------- Tests --------

    @Test
    @DisplayName("Debe guardar un pasajero correctamente")
    void shouldSavePassenger() {
        Passenger passenger = buildPassenger("Carlos Ruiz", "999999999", null);

        Passenger saved = passengerRepository.save(passenger);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Carlos Ruiz");
        assertThat(saved.getDocumentNumber()).isEqualTo("999999999");
    }

    @Test
    @DisplayName("Debe encontrar pasajeros por userId")
    void shouldFindPassengersByUserId() {
        User user = givenUser();

        Passenger p1 = givenPassenger(user, "111");
        Passenger p2 = givenPassenger(user, "222");

        List<Passenger> found = passengerRepository.findByUserId(user.getId());

        assertThat(found).hasSize(2);
        assertThat(found)
                .extracting(Passenger::getDocumentNumber)
                .containsExactlyInAnyOrder("111", "222");
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el usuario no tiene pasajeros")
    void shouldReturnEmptyWhenUserHasNoPassengers() {
        User user = givenUser();

        List<Passenger> found = passengerRepository.findByUserId(user.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar pasajero por número de documento")
    void shouldFindPassengerByDocumentNumber() {
        Passenger saved = givenPassenger(null, "555555555");

        Optional<Passenger> found = passengerRepository.findByDocumentNumber("555555555");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getFullName()).isEqualTo("Maria López");
    }

    @Test
    @DisplayName("Debe retornar empty si el documento no existe")
    void shouldReturnEmptyIfDocumentNotExists() {
        Optional<Passenger> found = passengerRepository.findByDocumentNumber("999999999");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar true si el documento existe")
    void shouldReturnTrueIfDocumentExists() {
        givenPassenger(null, "123123123");

        boolean exists = passengerRepository.existsByDocumentNumber("123123123");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false si el documento no existe")
    void shouldReturnFalseIfDocumentNotExists() {
        boolean exists = passengerRepository.existsByDocumentNumber("000000000");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe guardar pasajero con campos opcionales nulos")
    void shouldSavePassengerWithNullOptionalFields() {
        Passenger passenger = Passenger.builder()
                .fullName("Test Passenger")
                .documentNumber(null)
                .documentType(null)
                .birthDate(null)
                .phoneNumber(null)
                .createdAt(OffsetDateTime.now())
                .user(null)
                .build();

        Passenger saved = passengerRepository.save(passenger);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Test Passenger");
        assertThat(saved.getDocumentNumber()).isNull();
        assertThat(saved.getUser()).isNull();
    }
}
