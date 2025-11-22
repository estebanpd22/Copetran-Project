package co.unimagdalena.api;

import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.AssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssignmentController.class)
@DisplayName("AssignmentControllerTest")
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentService assignmentService;

    private AssignmentResponse assignmentResponse;
    private AssignmentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        assignmentResponse = new AssignmentResponse(
                1L,
                true,
                LocalDateTime.of(2024, 11, 21, 10, 0),
                1L,
                1L,
                null
        );

        updateRequest = new AssignmentUpdateRequest(true);
    }

    // =====================================================================
    // ASSIGN DRIVER TO TRIP - POST /api/v1/assignments/driver
    // =====================================================================

    @Test
    @DisplayName("assignDriver_WithValidData_ReturnsOk200")
    void assignDriver_WithValidData_ReturnsOk200() throws Exception {
        Long tripId = 1L;
        Long driverId = 1L;

        when(assignmentService.assignDriverToTrip(tripId, driverId))
                .thenReturn(assignmentResponse);

        mockMvc.perform(post("/api/v1/assignments/driver")
                        .param("tripId", String.valueOf(tripId))
                        .param("driverId", String.valueOf(driverId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.checkListOk", is(true)))
                .andExpect(jsonPath("$.tripId", is(1)));

        verify(assignmentService, times(1)).assignDriverToTrip(tripId, driverId);
    }

    @Test
    @DisplayName("assignDriver_WithInvalidTripId_Returns404NotFound")
    void assignDriver_WithInvalidTripId_Returns404NotFound() throws Exception {
        Long tripId = 999L;
        Long driverId = 1L;

        when(assignmentService.assignDriverToTrip(tripId, driverId))
                .thenThrow(new NotFoundException("Trip not found"));

        mockMvc.perform(post("/api/v1/assignments/driver")
                        .param("tripId", String.valueOf(tripId))
                        .param("driverId", String.valueOf(driverId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).assignDriverToTrip(tripId, driverId);
    }

    @Test
    @DisplayName("assignDriver_WithInvalidDriverId_Returns404NotFound")
    void assignDriver_WithInvalidDriverId_Returns404NotFound() throws Exception {
        Long tripId = 1L;
        Long driverId = 999L;

        when(assignmentService.assignDriverToTrip(tripId, driverId))
                .thenThrow(new NotFoundException("Driver not found"));

        mockMvc.perform(post("/api/v1/assignments/driver")
                        .param("tripId", String.valueOf(tripId))
                        .param("driverId", String.valueOf(driverId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).assignDriverToTrip(tripId, driverId);
    }

    @Test
    @DisplayName("assignDriver_WithDriverConflict_Returns409Conflict")
    void assignDriver_WithDriverConflict_Returns409Conflict() throws Exception {
        Long tripId = 1L;
        Long driverId = 1L;

        when(assignmentService.assignDriverToTrip(tripId, driverId))
                .thenThrow(new IllegalStateException("Driver has conflicting trip"));

        mockMvc.perform(post("/api/v1/assignments/driver")
                        .param("tripId", String.valueOf(tripId))
                        .param("driverId", String.valueOf(driverId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(assignmentService, times(1)).assignDriverToTrip(tripId, driverId);
    }

    @Test
    @DisplayName("assignDriver_WithInvalidRole_Returns422UnprocessableEntity")
    void assignDriver_WithInvalidRole_Returns422UnprocessableEntity() throws Exception {
        Long tripId = 1L;
        Long driverId = 1L;

        when(assignmentService.assignDriverToTrip(tripId, driverId))
                .thenThrow(new IllegalArgumentException("User is not a driver"));

        mockMvc.perform(post("/api/v1/assignments/driver")
                        .param("tripId", String.valueOf(tripId))
                        .param("driverId", String.valueOf(driverId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());

        verify(assignmentService, times(1)).assignDriverToTrip(tripId, driverId);
    }

    // =====================================================================
    // ASSIGN BUS TO TRIP - POST /api/v1/assignments/bus
    // =====================================================================

    @Test
    @DisplayName("assignBus_WithValidData_ReturnsOk200")
    void assignBus_WithValidData_ReturnsOk200() throws Exception {
        Long tripId = 1L;
        Long busId = 1L;

        when(assignmentService.assignBusToTrip(tripId, busId))
                .thenReturn(assignmentResponse);

        mockMvc.perform(post("/api/v1/assignments/bus")
                        .param("tripId", String.valueOf(tripId))
                        .param("busId", String.valueOf(busId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tripId", is(1)));

        verify(assignmentService, times(1)).assignBusToTrip(tripId, busId);
    }

    @Test
    @DisplayName("assignBus_WithInvalidTripId_Returns404NotFound")
    void assignBus_WithInvalidTripId_Returns404NotFound() throws Exception {
        Long tripId = 999L;
        Long busId = 1L;

        when(assignmentService.assignBusToTrip(tripId, busId))
                .thenThrow(new NotFoundException("Trip not found"));

        mockMvc.perform(post("/api/v1/assignments/bus")
                        .param("tripId", String.valueOf(tripId))
                        .param("busId", String.valueOf(busId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).assignBusToTrip(tripId, busId);
    }

    @Test
    @DisplayName("assignBus_WithInvalidBusId_Returns404NotFound")
    void assignBus_WithInvalidBusId_Returns404NotFound() throws Exception {
        Long tripId = 1L;
        Long busId = 999L;

        when(assignmentService.assignBusToTrip(tripId, busId))
                .thenThrow(new NotFoundException("Bus not found"));

        mockMvc.perform(post("/api/v1/assignments/bus")
                        .param("tripId", String.valueOf(tripId))
                        .param("busId", String.valueOf(busId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).assignBusToTrip(tripId, busId);
    }

    @Test
    @DisplayName("assignBus_WithBusConflict_Returns409Conflict")
    void assignBus_WithBusConflict_Returns409Conflict() throws Exception {
        Long tripId = 1L;
        Long busId = 1L;

        when(assignmentService.assignBusToTrip(tripId, busId))
                .thenThrow(new IllegalStateException("Bus has conflicting trip"));

        mockMvc.perform(post("/api/v1/assignments/bus")
                        .param("tripId", String.valueOf(tripId))
                        .param("busId", String.valueOf(busId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(assignmentService, times(1)).assignBusToTrip(tripId, busId);
    }

    // =====================================================================
    // UPDATE ASSIGNMENT - PATCH /api/v1/assignments/{id}
    // =====================================================================

    @Test
    @DisplayName("update_WithValidData_ReturnsNoContent204")
    void update_WithValidData_ReturnsNoContent204() throws Exception {
        Long assignmentId = 1L;

        doNothing().when(assignmentService).updateAssignment(assignmentId, updateRequest);

        mockMvc.perform(patch("/api/v1/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent());

        verify(assignmentService, times(1)).updateAssignment(assignmentId, updateRequest);
    }

    @Test
    @DisplayName("update_WithInvalidId_Returns404NotFound")
    void update_WithInvalidId_Returns404NotFound() throws Exception {
        Long assignmentId = 999L;

        doThrow(new NotFoundException("Assignment not found"))
                .when(assignmentService).updateAssignment(assignmentId, updateRequest);

        mockMvc.perform(patch("/api/v1/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).updateAssignment(assignmentId, updateRequest);
    }

    // =====================================================================
    // DELETE ASSIGNMENT - DELETE /api/v1/assignments/{id}
    // =====================================================================

    @Test
    @DisplayName("delete_WithValidId_ReturnsNoContent204")
    void delete_WithValidId_ReturnsNoContent204() throws Exception {
        Long assignmentId = 1L;

        doNothing().when(assignmentService).deleteAssignment(assignmentId);

        mockMvc.perform(delete("/api/v1/assignments/{id}", assignmentId))
                .andExpect(status().isNoContent());

        verify(assignmentService, times(1)).deleteAssignment(assignmentId);
    }

    @Test
    @DisplayName("delete_WithInvalidId_Returns404NotFound")
    void delete_WithInvalidId_Returns404NotFound() throws Exception {
        Long assignmentId = 999L;

        doThrow(new NotFoundException("Assignment not found"))
                .when(assignmentService).deleteAssignment(assignmentId);

        mockMvc.perform(delete("/api/v1/assignments/{id}", assignmentId))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).deleteAssignment(assignmentId);
    }

    // =====================================================================
    // GET ASSIGNMENT BY ID - GET /api/v1/assignments/{id}
    // =====================================================================

    @Test
    @DisplayName("get_WithValidId_ReturnsOk200")
    void get_WithValidId_ReturnsOk200() throws Exception {
        Long assignmentId = 1L;

        when(assignmentService.getAssignment(assignmentId))
                .thenReturn(assignmentResponse);

        mockMvc.perform(get("/api/v1/assignments/{id}", assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.checkListOk", is(true)));

        verify(assignmentService, times(1)).getAssignment(assignmentId);
    }

    @Test
    @DisplayName("get_WithInvalidId_Returns404NotFound")
    void get_WithInvalidId_Returns404NotFound() throws Exception {
        Long assignmentId = 999L;

        when(assignmentService.getAssignment(assignmentId))
                .thenThrow(new NotFoundException("Assignment not found"));

        mockMvc.perform(get("/api/v1/assignments/{id}", assignmentId))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).getAssignment(assignmentId);
    }

    // =====================================================================
    // GET ASSIGNMENT BY TRIP ID - GET /api/v1/assignments/trip/{tripId}
    // =====================================================================

    @Test
    @DisplayName("getByTrip_WithValidTripId_ReturnsOk200")
    void getByTrip_WithValidTripId_ReturnsOk200() throws Exception {
        Long tripId = 1L;

        when(assignmentService.getAssignmentByTripId(tripId))
                .thenReturn(assignmentResponse);

        mockMvc.perform(get("/api/v1/assignments/trip/{tripId}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tripId", is(1)));

        verify(assignmentService, times(1)).getAssignmentByTripId(tripId);
    }

    @Test
    @DisplayName("getByTrip_WithInvalidTripId_Returns404NotFound")
    void getByTrip_WithInvalidTripId_Returns404NotFound() throws Exception {
        Long tripId = 999L;

        when(assignmentService.getAssignmentByTripId(tripId))
                .thenThrow(new NotFoundException("Assignment not found for trip"));

        mockMvc.perform(get("/api/v1/assignments/trip/{tripId}", tripId))
                .andExpect(status().isNotFound());

        verify(assignmentService, times(1)).getAssignmentByTripId(tripId);
    }

    // =====================================================================
    // GET ASSIGNMENTS BY DRIVER ID - GET /api/v1/assignments/driver/{driverId}
    // =====================================================================

    @Test
    @DisplayName("getByDriver_WithValidDriverId_ReturnsOk200")
    void getByDriver_WithValidDriverId_ReturnsOk200() throws Exception {
        Long driverId = 1L;
        List<AssignmentResponse> assignments = List.of(assignmentResponse);

        when(assignmentService.getAssignmentByDriverId(driverId))
                .thenReturn(assignments);

        mockMvc.perform(get("/api/v1/assignments/driver/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(assignmentService, times(1)).getAssignmentByDriverId(driverId);
    }

    @Test
    @DisplayName("getByDriver_WithNoAssignments_ReturnsOk200WithEmptyList")
    void getByDriver_WithNoAssignments_ReturnsOk200WithEmptyList() throws Exception {
        Long driverId = 1L;

        when(assignmentService.getAssignmentByDriverId(driverId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/assignments/driver/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(assignmentService, times(1)).getAssignmentByDriverId(driverId);
    }

    // =====================================================================
    // CHECK DRIVER CONFLICT - GET /api/v1/assignments/check-conflict
    // =====================================================================

    @Test
    @DisplayName("checkDriverConflict_WithNoConflict_ReturnsOk200WithFalse")
    void checkDriverConflict_WithNoConflict_ReturnsOk200WithFalse() throws Exception {
        Long driverId = 1L;
        OffsetDateTime start = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime end = start.plusHours(2);

        when(assignmentService.checkDriverConflict(driverId, start, end))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/assignments/check-conflict")
                        .param("driverId", String.valueOf(driverId))
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(assignmentService, times(1)).checkDriverConflict(driverId, start, end);
    }

    @Test
    @DisplayName("checkDriverConflict_WithConflict_ReturnsOk200WithTrue")
    void checkDriverConflict_WithConflict_ReturnsOk200WithTrue() throws Exception {
        Long driverId = 1L;
        OffsetDateTime start = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime end = start.plusHours(2);

        when(assignmentService.checkDriverConflict(driverId, start, end))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/assignments/check-conflict")
                        .param("driverId", String.valueOf(driverId))
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(assignmentService, times(1)).checkDriverConflict(driverId, start, end);
    }
}
