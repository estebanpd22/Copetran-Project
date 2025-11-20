package co.unimagdalena.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            UserInfo user
    ) {
        public record UserInfo(
                Long id,
                String email,
                String name,
                String role
        ) {}
    }

    public record RefreshTokenRequest(
            @NotBlank String refreshToken
    ) {}

    public record RefreshTokenResponse(
            String accessToken,
            String tokenType,
            Long expiresIn
    ) {}
}