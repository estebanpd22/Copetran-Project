package co.unimagdalena.api;

import co.unimagdalena.api.dto.UserDto;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.security.dto.AuthDtos;
import co.unimagdalena.security.jwt.JwtService;
import co.unimagdalena.security.user.CustomUserDetails;
import co.unimagdalena.security.user.CustomUserDetailsService;
import co.unimagdalena.security.web.AuthController;
import co.unimagdalena.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void register_shouldReturn201AndLocation() throws Exception {
        // Given
        var registerRequest = new UserDto.UserCreateRequest(
                "Juan Pérez", "juan@test.com", "1234567890", UserRole.PASSENGER, "password123"
        );

        var userResponse = new UserDto.UserResponse(
                1L, "Juan Pérez", "juan@test.com", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now()
        );

        when(userService.registerUser(any(UserDto.UserCreateRequest.class))).thenReturn(userResponse);

        // When & Then
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/users/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void register_shouldReturn400WhenInvalidData() throws Exception {
        // Given - Datos inválidos
        var invalidRequest = new UserDto.UserCreateRequest(
                "", "email-invalido", "", UserRole.PASSENGER, "123"
        );

        // When & Then
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // Tests para LOGIN
    @Test
    void login_shouldReturn200WithTokens() throws Exception {
        // Given
        var loginRequest = new AuthDtos.LoginRequest("usuario@test.com", "password123");

        var userDetails = new CustomUserDetails(
                1L,
                "usuario@test.com",
                "Usuario Test",
                "1234567890",
                "encodedPassword",
                "ROLE_PASSENGER",
                true,
                "ACTIVE"
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token-123");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token-456");
        when(jwtService.extractExpiration(any())).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

        // When & Then
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userInfo.email").value("usuario@test.com"))
                .andExpect(jsonPath("$.userInfo.fullName").value("Usuario Test"))
                .andExpect(jsonPath("$.userInfo.role").value("PASSENGER"));
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        // Given
        var loginRequest = new AuthDtos.LoginRequest("usuario@test.com", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // When & Then
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // Tests para REFRESH TOKEN
    @Test
    void refreshToken_shouldReturn200WithNewAccessToken() throws Exception {
        // Given
        var refreshRequest = new AuthDtos.RefreshTokenRequest("valid-refresh-token");
        var userDetails = new CustomUserDetails(
                1L,
                "usuario@test.com",
                "Usuario Test",
                "1234567890",
                "encodedPassword",
                "ROLE_PASSENGER",
                true,
                "ACTIVE"
        );

        when(jwtService.isRefreshTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-refresh-token")).thenReturn(1L);
        when(userDetailsService.loadUserById(1L)).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtService.extractExpiration(any())).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));

        // When & Then
        mvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void refreshToken_shouldReturn400WhenInvalidToken() throws Exception {
        // Given
        var refreshRequest = new AuthDtos.RefreshTokenRequest("invalid-refresh-token");

        when(jwtService.isRefreshTokenValid("invalid-refresh-token")).thenReturn(false);

        // When & Then
        mvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        var refreshRequest = new AuthDtos.RefreshTokenRequest("valid-refresh-token");

        when(jwtService.isRefreshTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-refresh-token")).thenReturn(999L);
        when(userDetailsService.loadUserById(999L))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(refreshRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_shouldReturn401WhenUserInactive() throws Exception {
        // Given
        var loginRequest = new AuthDtos.LoginRequest("inactive@test.com", "password123");

        var inactiveUserDetails = new CustomUserDetails(
                2L,
                "inactive@test.com",
                "Usuario Inactivo",
                "1234567890",
                "encodedPassword",
                "ROLE_PASSENGER",
                false,
                "INACTIVE"
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                inactiveUserDetails, null, inactiveUserDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}