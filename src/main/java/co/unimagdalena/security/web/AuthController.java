package co.unimagdalena.security.web;

import co.unimagdalena.api.dto.UserDto;
import co.unimagdalena.security.dto.AuthDtos;
import co.unimagdalena.security.jwt.JwtService;
import co.unimagdalena.security.user.CustomUserDetails;
import co.unimagdalena.security.user.CustomUserDetailsService;
import co.unimagdalena.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<UserDto.UserResponse> register(
            @Valid @RequestBody UserDto.UserCreateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        log.info("New User register: {}", request.email());

        UserDto.UserResponse userCreated = userService.registerUser(request);

        var location = uriBuilder.path("/api/users/{id}")
                .buildAndExpand(userCreated.id())
                .toUri();

        return ResponseEntity.created(location).body(userCreated);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request
    ) {
        log.info("Login attempt: {}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.extractExpiration(accessToken).getTime() / 1000,
                new AuthDtos.AuthResponse.UserInfo(
                        userDetails.getUserId(),
                        userDetails.getEmail(),
                        userDetails.getFullName(),
                        userDetails.getRole().replace("ROLE_", "")
                )
        );

        log.info("Login successful: {} with role {}", request.email(), userDetails.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthDtos.RefreshTokenResponse> refreshToken(
            @Valid @RequestBody AuthDtos.RefreshTokenRequest request
    ) {
        log.info("Refresh token request");

        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            log.error("Refresh token invalid or expired");
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        Long userId = jwtService.extractUserId(request.refreshToken());
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(userId);

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        AuthDtos.RefreshTokenResponse response = new AuthDtos.RefreshTokenResponse(
                newAccessToken,
                "Bearer",
                jwtService.extractExpiration(newAccessToken).getTime() / 1000
        );

        log.info("Token refreshed for user: {}", userDetails.getEmail());

        return ResponseEntity.ok(response);
    }
}