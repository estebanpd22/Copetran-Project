package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserDto {

    public record UserCreateRequest(
            @NotNull String fullName,
            @NotNull @Email String email,
            @NotNull String phone,
            @NotNull UserRole role,
            @NotNull String password
    ) implements Serializable {}

    public record UserUpdateRequest(
            String fullName,
            @Email String email,
            String phone,
            UserRole role,
            String password
    ) implements Serializable {}

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            UserRole role,
            UserStatus status,
            LocalDateTime createdAt
    ) implements Serializable {}

    public record UserSummary(
            Long id,
            String fullName,
            String email,
            String phone
    ) implements Serializable {}
}
