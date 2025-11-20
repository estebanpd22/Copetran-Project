package co.unimagdalena.services;

import co.unimagdalena.api.dto.UserDto.*;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.UserServiceImpl;
import co.unimagdalena.services.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Test Suite")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateRequest userCreateRequest;
    private UserResponse userResponse;
    private EmployeeCreateRequest employeeCreateRequest;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        userCreateRequest = createUserCreateRequest();
        userResponse = createUserResponse();
        employeeCreateRequest = createEmployeeCreateRequest();
    }

    // ============== registerUser Tests ==============

    @Test
    @DisplayName("Should register user successfully with valid data")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "john@example.com",
                "3101234567",
                UserRole.PASSENGER,
                "SecurePass123"
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName(request.fullName());
        savedUser.setEmail(request.email());
        savedUser.setPhone(request.phone());
        savedUser.setRole(UserRole.PASSENGER);
        savedUser.setPasswordHash("encodedPassword");
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userMapper.toEntity(request)).thenReturn(new User());
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhone(request.phone())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(new UserResponse(
                1L, "John Doe", "john@example.com", "3101234567",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("john@example.com", response.email());
        assertEquals(UserRole.PASSENGER, response.role());
        verify(userRepository).save(any(User.class));
        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    @DisplayName("Should throw exception when email already exists during registration")
    void shouldThrowExceptionWhenEmailExistsDuringRegistration() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "existing@example.com",
                "3101234567",
                UserRole.PASSENGER,
                "SecurePass123"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when phone already exists during registration")
    void shouldThrowExceptionWhenPhoneExistsDuringRegistration() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "john@example.com",
                "3101234567",
                UserRole.PASSENGER,
                "SecurePass123"
        );

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setPhone(request.phone());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhone(request.phone())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception with invalid email format")
    void shouldThrowExceptionWithInvalidEmailFormat() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "invalid-email",
                "3101234567",
                UserRole.PASSENGER,
                "SecurePass123"
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw exception with invalid phone format")
    void shouldThrowExceptionWithInvalidPhoneFormat() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "john@example.com",
                "1234567890",  // Invalid format - doesn't start with 3
                UserRole.PASSENGER,
                "SecurePass123"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should register user with null optional phone")
    void shouldRegisterUserWithNullPhone() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "john@example.com",
                null,
                UserRole.PASSENGER,
                "SecurePass123"
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPhone(null);
        savedUser.setRole(UserRole.PASSENGER);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userMapper.toEntity(request)).thenReturn(new User());
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(new UserResponse(
                1L, "John Doe", "john@example.com", null,
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertNull(response.phone());
    }

    // ============== createEmployee Tests ==============

    @Test
    @DisplayName("Should create employee successfully with valid data")
    void shouldCreateEmployeeSuccessfully() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "admin@example.com",
                "Admin User",
                "3101234567",
                UserRole.ADMIN
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.email());
        savedUser.setFullName(request.name());
        savedUser.setPhone(request.phone());
        savedUser.setRole(UserRole.ADMIN);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhone(request.phone())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(new UserResponse(
                1L, "Admin User", "admin@example.com", "3101234567",
                UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.createEmployee(request);

        // Assert
        assertNotNull(response);
        assertEquals(UserRole.ADMIN, response.role());
        assertNotEquals(UserRole.PASSENGER, response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to create PASSENGER as employee")
    void shouldThrowExceptionWhenCreatingPassengerAsEmployee() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "passenger@example.com",
                "Passenger User",
                "3101234567",
                UserRole.PASSENGER
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createEmployee(request);
        });

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email exists for employee creation")
    void shouldThrowExceptionWhenEmailExistsDuringEmployeeCreation() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "existing@example.com",
                "Employee",
                "3101234567",
                UserRole.CLERK
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createEmployee(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when phone exists for employee creation")
    void shouldThrowExceptionWhenPhoneExistsDuringEmployeeCreation() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "newadmin@example.com",
                "New Admin",
                "3101234567",
                UserRole.ADMIN
        );

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setPhone(request.phone());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhone(request.phone())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createEmployee(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should create employee with null optional phone")
    void shouldCreateEmployeeWithNullPhone() {
        // Arrange
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "clerk@example.com",
                "Clerk User",
                null,
                UserRole.CLERK
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("clerk@example.com");
        savedUser.setFullName("Clerk User");
        savedUser.setPhone(null);
        savedUser.setRole(UserRole.CLERK);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(new UserResponse(
                1L, "Clerk User", "clerk@example.com", null,
                UserRole.CLERK, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.createEmployee(request);

        // Assert
        assertNotNull(response);
        assertNull(response.phone());
    }

    // ============== updateUser Tests ==============

    @Test
    @DisplayName("Should update user successfully with valid data")
    void shouldUpdateUserSuccessfully() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "Updated Name",
                null,
                "3109876543",
                null,
                null
        );

        User user = new User();
        user.setId(userId);
        user.setFullName("Old Name");
        user.setPhone("3101234567");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByPhone("3109876543")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.updateUser(userId, request);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        verify(userMapper).updateEntity(request, user);
    }

    @Test
    @DisplayName("Should throw exception when user not found during update")
    void shouldThrowExceptionWhenUserNotFoundDuringUpdate() {
        // Arrange
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest(
                "Updated Name",
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.updateUser(userId, request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new phone already in use")
    void shouldThrowExceptionWhenNewPhoneAlreadyInUse() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "John Doe",
                null,
                "3108888888",
                null,
                null
        );

        User user = new User();
        user.setId(userId);
        user.setPhone("3101234567");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setPhone("3108888888");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByPhone("3108888888")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    // ============== changePassword Tests ==============

    @Test
    @DisplayName("Should change password successfully with valid old password")
    void shouldChangePasswordSuccessfully() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "OldPass123";
        String newPassword = "NewPass456";
        String encodedOldPassword = "encodedOldPass123";
        String encodedNewPassword = "encodedNewPass456";

        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedOldPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.changePassword(userId, oldPassword, newPassword);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during password change")
    void shouldThrowExceptionWhenUserNotFoundDuringPasswordChange() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.changePassword(userId, "OldPass", "NewPass");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
        // Arrange
        Long userId = 1L;
        String encodedPassword = "encodedCorrectPass";
        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPass", encodedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, "WrongOldPass", "NewPass456");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new password equals old password")
    void shouldThrowExceptionWhenNewPasswordEqualsOld() {
        // Arrange
        Long userId = 1L;
        String samePassword = "SamePass123";
        String encodedPassword = "encodedSamePass123";

        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(samePassword, encodedPassword)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, samePassword, samePassword);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new password is weak")
    void shouldThrowExceptionWhenNewPasswordIsWeak() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "OldPass123";
        String weakPassword = "weak";
        String encodedOldPassword = "encodedOldPass123";

        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, oldPassword, weakPassword);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when password missing uppercase")
    void shouldThrowExceptionWhenPasswordMissingUppercase() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "OldPass123";
        String newPassword = "newpass123";  // Missing uppercase
        String encodedOldPassword = "encodedOldPass123";

        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, oldPassword, newPassword);
        });
    }

    @Test
    @DisplayName("Should throw exception when password missing lowercase")
    void shouldThrowExceptionWhenPasswordMissingLowercase() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "OldPass123";
        String newPassword = "NEWPASS123";  // Missing lowercase
        String encodedOldPassword = "encodedOldPass123";

        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, oldPassword, newPassword);
        });
    }

    @Test
    @DisplayName("Should throw exception when password missing number")
    void shouldThrowExceptionWhenPasswordMissingNumber() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "OldPass123";
        String newPassword = "NewPassAbc";  // Missing number
        String encodedOldPassword = "encodedOldPass123";

        User user = new User();
        user.setId(userId);
        user.setPasswordHash(encodedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, oldPassword, newPassword);
        });
    }

    // ============== desactivateUser Tests ==============

    @Test
    @DisplayName("Should deactivate active user successfully")
    void shouldDeactivateActiveUserSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.desactivateUser(userId);

        // Assert
        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to deactivate already inactive user")
    void shouldThrowExceptionWhenDeactivatingAlreadyInactiveUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            userService.desactivateUser(userId);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during deactivation")
    void shouldThrowExceptionWhenUserNotFoundDuringDeactivation() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.desactivateUser(userId);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    // ============== reactivateUser Tests ==============

    @Test
    @DisplayName("Should reactivate inactive user successfully")
    void shouldReactivateInactiveUserSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.reactivateUser(userId);

        // Assert
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to reactivate already active user")
    void shouldThrowExceptionWhenReactivatingAlreadyActiveUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            userService.reactivateUser(userId);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during reactivation")
    void shouldThrowExceptionWhenUserNotFoundDuringReactivation() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.reactivateUser(userId);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    // ============== getUserById Tests ==============

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse(
                userId, "John Doe", "john@example.com", "3101234567",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.id());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }

    // ============== getUserByEmail Tests ==============

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmailSuccessfully() {
        // Arrange
        String email = "john@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse(
                1L, "John Doe", email, "3101234567",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.getUserByEmail(email);

        // Assert
        assertNotNull(response);
        assertEquals(email, response.email());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Arrange
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.getUserByEmail(email);
        });
    }

    // ============== getUserByPhone Tests ==============

    @Test
    @DisplayName("Should get user by phone successfully")
    void shouldGetUserByPhoneSuccessfully() {
        // Arrange
        String phone = "3101234567";
        User user = new User();
        user.setId(1L);
        user.setPhone(phone);

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(new UserResponse(
                1L, "John Doe", "john@example.com", phone,
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        UserResponse response = userService.getUserByPhone(phone);

        // Assert
        assertNotNull(response);
        assertEquals(phone, response.phone());
        verify(userRepository).findByPhone(phone);
    }

    @Test
    @DisplayName("Should throw exception when user not found by phone")
    void shouldThrowExceptionWhenUserNotFoundByPhone() {
        // Arrange
        String phone = "9999999999";

        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            userService.getUserByPhone(phone);
        });
    }

    // ============== getAllUsersByRole Tests ==============

    @Test
    @DisplayName("Should get all users by role successfully")
    void shouldGetAllUsersByRoleSuccessfully() {
        // Arrange
        UserRole role = UserRole.ADMIN;
        User admin1 = new User();
        admin1.setId(1L);
        admin1.setRole(UserRole.ADMIN);

        User admin2 = new User();
        admin2.setId(2L);
        admin2.setRole(UserRole.ADMIN);

        List<User> adminList = Arrays.asList(admin1, admin2);

        when(userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE))
                .thenReturn(adminList);
        when(userMapper.toResponse(admin1)).thenReturn(new UserResponse(
                1L, "Admin1", "admin1@example.com", "3101111111",
                UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.now()
        ));
        when(userMapper.toResponse(admin2)).thenReturn(new UserResponse(
                2L, "Admin2", "admin2@example.com", "3102222222",
                UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        List<UserResponse> responses = userService.getAllUsersByRole(role);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(userRepository).findByRoleAndStatus(role, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return empty list when no users found for role")
    void shouldReturnEmptyListWhenNoUsersFoundForRole() {
        // Arrange
        UserRole role = UserRole.DRIVER;

        when(userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE))
                .thenReturn(Arrays.asList());

        // Act
        List<UserResponse> responses = userService.getAllUsersByRole(role);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(userRepository).findByRoleAndStatus(role, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get all passengers successfully")
    void shouldGetAllPassengersSuccessfully() {
        // Arrange
        UserRole role = UserRole.PASSENGER;
        User passenger = new User();
        passenger.setId(1L);
        passenger.setRole(UserRole.PASSENGER);

        List<User> passengers = Arrays.asList(passenger);

        when(userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE))
                .thenReturn(passengers);
        when(userMapper.toResponse(passenger)).thenReturn(new UserResponse(
                1L, "Passenger", "passenger@example.com", "3101234567",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        ));

        // Act
        List<UserResponse> responses = userService.getAllUsersByRole(role);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    // ============== Helper Methods ==============

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("3101234567");
        user.setRole(UserRole.PASSENGER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(
                "Test User",
                "test@example.com",
                "3101234567",
                UserRole.PASSENGER,
                "TestPass123"
        );
    }

    private UserResponse createUserResponse() {
        return new UserResponse(
                1L,
                "Test User",
                "test@example.com",
                "3101234567",
                UserRole.PASSENGER,
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    private EmployeeCreateRequest createEmployeeCreateRequest() {
        return new EmployeeCreateRequest(
                "employee@example.com",
                "Test Employee",
                "3109876543",
                UserRole.ADMIN
        );
    }
}
