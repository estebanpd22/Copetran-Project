package co.unimagdalena.api.controllers;

import co.unimagdalena.api.AssignmentController;
import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.api.dto.TripDto;
import co.unimagdalena.api.dto.UserDto;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.services.AssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@Import(TestSecurityConfig.class)
class AssignmentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean AssignmentService service;

    @Test
    void assignDriver_shouldReturn200() throws Exception {
        var tripSummary = new TripDto.TripSummary(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2), co.unimagdalena.domine.entities.TripStatus.SCHEDULED, null);
        var driverSummary = new UserDto.UserSummary(1L, "John Driver", "driver@test.com", "1234567890");
        var dispatcherSummary = new UserDto.UserSummary(2L, "Jane Dispatcher", "dispatcher@test.com", "0987654321");
        var resp = new AssignmentResponse(1L, true, LocalDateTime.now(), tripSummary, driverSummary, dispatcherSummary);

        when(service.assignDriverToTrip(1L, 1L)).thenReturn(resp);

        mvc.perform(post("/api/v1/assignments/driver")
                        .param("tripId", "1")
                        .param("driverId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.checkListOk").value(true));
    }

    @Test
    void assignBus_shouldReturn200() throws Exception {
        var tripSummary = new TripDto.TripSummary(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2), co.unimagdalena.domine.entities.TripStatus.SCHEDULED, null);
        var driverSummary = new UserDto.UserSummary(1L, "John Driver", "driver@test.com", "1234567890");
        var dispatcherSummary = new UserDto.UserSummary(2L, "Jane Dispatcher", "dispatcher@test.com", "0987654321");
        var resp = new AssignmentResponse(1L, true, LocalDateTime.now(), tripSummary, driverSummary, dispatcherSummary);

        when(service.assignBusToTrip(1L, 1L)).thenReturn(resp);

        mvc.perform(post("/api/v1/assignments/bus")
                        .param("tripId", "1")
                        .param("busId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new AssignmentUpdateRequest(false, LocalDateTime.now(), 1L, 1L, 2L);

        mvc.perform(patch("/api/v1/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateAssignment(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/assignments/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteAssignment(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var tripSummary = new TripDto.TripSummary(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2), co.unimagdalena.domine.entities.TripStatus.SCHEDULED, null);
        var driverSummary = new UserDto.UserSummary(1L, "John Driver", "driver@test.com", "1234567890");
        var dispatcherSummary = new UserDto.UserSummary(2L, "Jane Dispatcher", "dispatcher@test.com", "0987654321");
        var resp = new AssignmentResponse(1L, true, LocalDateTime.now(), tripSummary, driverSummary, dispatcherSummary);

        when(service.getAssignment(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/assignments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByTrip_shouldReturn200() throws Exception {
        var tripSummary = new TripDto.TripSummary(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2), co.unimagdalena.domine.entities.TripStatus.SCHEDULED, null);
        var driverSummary = new UserDto.UserSummary(1L, "John Driver", "driver@test.com", "1234567890");
        var dispatcherSummary = new UserDto.UserSummary(2L, "Jane Dispatcher", "dispatcher@test.com", "0987654321");
        var resp = new AssignmentResponse(1L, true, LocalDateTime.now(), tripSummary, driverSummary, dispatcherSummary);

        when(service.getAssignmentByTripId(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/assignments/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByDriver_shouldReturn200() throws Exception {
        var assignments = List.of(
                new AssignmentResponse(1L, true, LocalDateTime.now(), null, null, null),
                new AssignmentResponse(2L, true, LocalDateTime.now(), null, null, null)
        );

        when(service.getAssignmentByDriverId(1L)).thenReturn(assignments);

        mvc.perform(get("/api/v1/assignments/driver/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void checkDriverConflict_shouldReturn200() throws Exception {
        when(service.checkDriverConflict(eq(1L), any(), any())).thenReturn(false);

        mvc.perform(get("/api/v1/assignments/check-conflict")
                        .param("driverId", "1")
                        .param("start", OffsetDateTime.now().toString())
                        .param("end", OffsetDateTime.now().plusHours(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
