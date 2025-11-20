package co.unimagdalena.api;

import co.unimagdalena.api.UserController;
import co.unimagdalena.api.dto.UserDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean UserService service;

    @Test
    void createEmployee_shouldReturn201AndLocation() throws Exception {
        var req = new EmployeeCreateRequest("admin@test.com", "Admin User", "1234567890",
                UserRole.ADMIN);
        var resp = new UserResponse(1L, "Admin User", "admin@test.com", "1234567890",
                UserRole.ADMIN, UserStatus.ACTIVE, LocalDateTime.now());

        when(service.createEmployee(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/users/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/users/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new UserUpdateRequest("John Updated", "john@test.com", "1234567890",
                UserRole.PASSENGER, null);

        mvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateUser(eq(1L), any());
    }

    @Test
    void changePassword_shouldReturn204() throws Exception {
        mvc.perform(patch("/api/v1/users/1/change-password")
                        .param("oldPassword", "oldpass123")
                        .param("newPassword", "newpass123"))
                .andExpect(status().isNoContent());

        verify(service).changePassword(1L, "oldpass123", "newpass123");
    }

    @Test
    void deactivate_shouldReturn204() throws Exception {
        mvc.perform(patch("/api/v1/users/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(service).desactivateUser(1L);
    }

    @Test
    void reactivate_shouldReturn204() throws Exception {
        mvc.perform(patch("/api/v1/users/1/reactivate"))
                .andExpect(status().isNoContent());

        verify(service).reactivateUser(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "John Doe", "john@test.com", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now());

        when(service.getUserById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void getByEmail_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "John Doe", "john@test.com", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now());

        when(service.getUserByEmail("john@test.com")).thenReturn(resp);

        mvc.perform(get("/api/v1/users/by-email/john@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void getByPhone_shouldReturn200() throws Exception {
        var resp = new UserResponse(1L, "John Doe", "john@test.com", "1234567890",
                UserRole.PASSENGER, UserStatus.ACTIVE, LocalDateTime.now());

        when(service.getUserByPhone("1234567890")).thenReturn(resp);

        mvc.perform(get("/api/v1/users/by-phone/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("1234567890"));
    }

    @Test
    void getByRole_shouldReturn200() throws Exception {
        var users = List.of(
                new UserResponse(1L, "Driver One", "driver1@test.com", "1234567890",
                        UserRole.DRIVER, UserStatus.ACTIVE, LocalDateTime.now()),
                new UserResponse(2L, "Driver Two", "driver2@test.com", "0987654321",
                        UserRole.DRIVER, UserStatus.ACTIVE, LocalDateTime.now())
        );

        when(service.getAllUsersByRole(UserRole.DRIVER)).thenReturn(users);

        mvc.perform(get("/api/v1/users/by-role/DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
