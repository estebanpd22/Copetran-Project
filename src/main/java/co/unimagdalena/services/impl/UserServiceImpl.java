package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.UserDto.*;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.UserService;
import co.unimagdalena.services.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(UserCreateRequest request) {
        log.debug("Registering new user with email: {} and role: {}", request.email(), request.role());

        validateEmail(request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.error("Email already exists: {}", request.email());
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        if (request.phone() != null) {
            validatePhone(request.phone());
            if (userRepository.findByPhone(request.phone()).isPresent()) {
                log.error("Phone already exists: {}", request.phone());
                throw new IllegalArgumentException("User with phone " + request.phone() + " already exists");
            }
        }

        User user = userMapper.toEntity(request);

        user.setRole(request.role());

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} and role: {}", savedUser.getId(), savedUser.getRole());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse createEmployee(EmployeeCreateRequest request) {
        log.debug("Creating employee with email: {} and role: {}", request.email(), request.role());

        validateEmail(request.email());

        if (request.role() == UserRole.PASSENGER) {
            log.error("Cannot create PASSENGER accounts with createEmployee method");
            throw new IllegalArgumentException(
                    "Cannot create PASSENGER accounts with this method. Use public registration endpoint."
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            log.error("Email already exists: {}", request.email());
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        if (request.phone() != null) {
            validatePhone(request.phone());
            if (userRepository.findByPhone(request.phone()).isPresent()) {
                log.error("Phone already exists: {}", request.phone());
                throw new IllegalArgumentException("User with phone " + request.phone() + " already exists");
            }
        }

        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.name());
        user.setPhone(request.phone());
        user.setRole(request.role());

        String tempPassword = generateTemporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));

        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        log.info("Employee created with ID: {} and role: {}. Temporary password: {}",
                savedUser.getId(), savedUser.getRole(), tempPassword);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.debug("Changing password for user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User with ID " + id + " not found");
                });

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.error("Invalid old password for user ID: {}", id);
            throw new IllegalArgumentException("Invalid old password");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            log.error("New password is the same as old password for user ID: {}", id);
            throw new IllegalArgumentException("New password must be different from old password");
        }

        validatePasswordStrength(newPassword);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserEntityById(Long id) {
        return userRepository.findById(id);
    }


    @Override
    public void desactivateUser(Long id) {
        log.debug("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User with ID " + id + " not found");
                });

        if (user.getStatus() == UserStatus.INACTIVE) {
            log.warn("User with ID {} is already inactive", id);
            throw new IllegalStateException("User with ID " + id + " is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("User deactivated successfully with ID: {}", id);
    }

    @Override
    public void reactivateUser(Long id) {
        log.debug("Reactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User with ID " + id + " not found");
                });

        if (user.getStatus() == UserStatus.ACTIVE) {
            log.warn("User with ID {} is already active", id);
            throw new IllegalStateException("User with ID " + id + " is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("User reactivated successfully with ID: {}", id);
    }

    @Override
    public void updateUser(Long id, UserUpdateRequest request) {
        log.debug("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User with ID " + id + " not found");
                });

        // Validar y actualizar teléfono
        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            validatePhone(request.phone());
            userRepository.findByPhone(request.phone()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new IllegalArgumentException("Phone " + request.phone() + " is already in use");
                }
            });
        }
        userMapper.updateEntity(request, user);
        userRepository.save(user);

        log.info("User updated successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new NotFoundException("User with ID " + id + " not found");
                });

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new NotFoundException("User with email " + email + " not found");
                });

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByPhone(String phone) {
        log.debug("Getting user by phone: {}", phone);

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.error("User not found with phone: {}", phone);
                    return new NotFoundException("User with phone " + phone + " not found");
                });

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersByRole(UserRole role) {
        log.debug("Getting all users by role: {}", role);

        List<User> users = userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE);

        log.info("Found {} users with role: {}", users.size(), role);

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // Métodos auxiliares privados
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return;  // Phone es opcional
        }

        String phoneRegex = "^3[0-9]{9}$";
        if (!phone.matches(phoneRegex)) {
            throw new IllegalArgumentException(
                    "Invalid phone format. Must be 10 digits starting with 3"
            );
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Validar que tenga al menos una mayúscula, minúscula y número
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder("Temp");

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
