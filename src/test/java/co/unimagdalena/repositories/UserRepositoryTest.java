package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private UserRepository userRepository;

    private User createUser(String fullName, String email, String phone, String passwordHash,
                            UserRole role, UserStatus status, LocalDateTime createdAt) {
        return userRepository.save(User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordHash)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar un usuario por ID")
    void shouldFindUserById() {
        // Given
        User user = createUser("Juan Pérez", "juan.perez@email.com", "3001234567",
                "hashedpassword", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());

        // When
        Optional<User> found = userRepository.findUserById(user.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
        assertThat(found.get().getFullName()).isEqualTo("Juan Pérez");
        assertThat(found.get().getEmail()).isEqualTo("juan.perez@email.com");
        assertThat(found.get().getPhone()).isEqualTo("3001234567");
        assertThat(found.get().getRole()).isEqualTo(UserRole.PASSENGER);
        assertThat(found.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe encontrar un usuario por email")
    void shouldFindByEmail() {
        // Given
        createUser("María López", "maria.lopez@email.com", "3009876543",
                "hashedpassword1", UserRole.ADMIN, UserStatus.ACTIVE,
                LocalDateTime.now());

        // When
        Optional<User> found = userRepository.findByEmail("maria.lopez@email.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("maria.lopez@email.com");
        assertThat(found.get().getFullName()).isEqualTo("María López");
        assertThat(found.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Debe encontrar un usuario por teléfono")
    void shouldFindByPhone() {
        // Given
        createUser("Carlos Ruiz", "carlos.ruiz@email.com", "3002345678",
                "hashedpassword2", UserRole.CLERK, UserStatus.ACTIVE,
                LocalDateTime.now());

        // When
        Optional<User> found = userRepository.findByPhone("3002345678");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isEqualTo("3002345678");
        assertThat(found.get().getFullName()).isEqualTo("Carlos Ruiz");
    }

    @Test
    @DisplayName("Debe verificar si existe un usuario por email")
    void shouldExistsByEmail() {
        // Given
        createUser("Ana Torres", "ana.torres@email.com", "3008765432",
                "hashedpassword3", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());

        // When
        boolean exists = userRepository.existsByEmail("ana.torres@email.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe encontrar usuarios por rol y estado")
    void shouldFindByRoleAndStatus() {
        // Given
        createUser("Pedro Gómez", "pedro.gomez@email.com", "3003456789",
                "hashedpassword4", UserRole.DRIVER, UserStatus.ACTIVE,
                LocalDateTime.now());
        createUser("Laura Díaz", "laura.diaz@email.com", "3007654321",
                "hashedpassword5", UserRole.DRIVER, UserStatus.ACTIVE,
                LocalDateTime.now());
        createUser("Luis Martínez", "luis.martinez@email.com", "3004567890",
                "hashedpassword6", UserRole.DRIVER, UserStatus.INACTIVE,
                LocalDateTime.now());

        // When
        List<User> activeDrivers = userRepository.findByRoleAndStatus(UserRole.DRIVER, UserStatus.ACTIVE);

        // Then
        assertThat(activeDrivers).hasSize(2);
        assertThat(activeDrivers).allMatch(u -> u.getRole() == UserRole.DRIVER &&
                u.getStatus() == UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe cambiar el estado de un usuario")
    void shouldChangeUserStatus() {
        // Given
        User user = createUser("Sofia García", "sofia.garcia@email.com", "3006543210",
                "hashedpassword7", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());

        // When
        userRepository.changeUserStatus(user.getId(), UserStatus.BLOCKED);
        userRepository.flush();

        // Then
        Optional<User> updated = userRepository.findUserById(user.getId());
        assertThat(updated.isPresent()).isTrue();
        assertThat(updated.get().getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe el usuario")
    void shouldReturnEmptyWhenUserNotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@email.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar false cuando el email no existe")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("notfound@email.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay usuarios con el rol y estado especificados")
    void shouldReturnEmptyListWhenNoUsersWithRoleAndStatus() {
        // Given
        createUser("Miguel Ángel", "miguel.angel@email.com", "3005678901",
                "hashedpassword8", UserRole.PASSENGER, UserStatus.ACTIVE,
                LocalDateTime.now());

        // When
        List<User> dispatchers = userRepository.findByRoleAndStatus(UserRole.DISPATCHER, UserStatus.ACTIVE);

        // Then
        assertThat(dispatchers).isEmpty();
    }
}
