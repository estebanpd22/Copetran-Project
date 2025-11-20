package co.unimagdalena.api.controllers;

import co.unimagdalena.api.SeatHoldController;
import co.unimagdalena.api.dto.SeatDto;
import co.unimagdalena.api.dto.SeatHoldDto.*;
import co.unimagdalena.api.dto.TripDto;
import co.unimagdalena.api.dto.UserDto;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.SeatHoldStatus;
import co.unimagdalena.domine.entities.SeatStatus;
import co.unimagdalena.domine.entities.SeatType;
import co.unimagdalena.services.SeatHoldService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatHoldController.class)
@Import(TestSecurityConfig.class)
class SeatHoldControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean SeatHoldService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var expiresAt = OffsetDateTime.now().plusMinutes(15);
        var req = new SeatHoldCreateRequest("A1", expiresAt, SeatHoldStatus.HOLD, 1L, 1L, 1L);
        var seatSummary = new SeatDto.SeatSummary(1L, 1, SeatType.STANDARD,
                new BigDecimal("50.00"), SeatStatus.AVAILABLE);
        var tripSummary = new TripDto.TripSummary(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2), co.unimagdalena.domine.entities.TripStatus.SCHEDULED, null);
        var userSummary = new UserDto.UserSummary(1L, "John Doe", "john@test.com", "1234567890");
        var resp = new SeatHoldResponse(1L, "A1", expiresAt, SeatHoldStatus.HOLD,
                seatSummary, tripSummary, userSummary);

        when(service.createSeatHold(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/seat-holds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/seat-holds/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var expiresAt = OffsetDateTime.now().plusMinutes(15);
        var seatSummary = new SeatDto.SeatSummary(1L, 1, SeatType.STANDARD,
                new BigDecimal("50.00"), SeatStatus.AVAILABLE);
        var tripSummary = new TripDto.TripSummary(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2), co.unimagdalena.domine.entities.TripStatus.SCHEDULED, null);
        var userSummary = new UserDto.UserSummary(1L, "John Doe", "john@test.com", "1234567890");
        var resp = new SeatHoldResponse(1L, "A1", expiresAt, SeatHoldStatus.HOLD,
                seatSummary, tripSummary, userSummary);

        when(service.getHoldById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/seat-holds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("A1"));
    }

    @Test
    void release_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/seat-holds/1"))
                .andExpect(status().isNoContent());

        verify(service).releaseSeatHold(1L);
    }

    @Test
    void getByTrip_shouldReturn200() throws Exception {
        var holds = List.of(
                new SeatHoldResponse(1L, "A1", OffsetDateTime.now().plusMinutes(15),
                        SeatHoldStatus.HOLD, null, null, null),
                new SeatHoldResponse(2L, "A2", OffsetDateTime.now().plusMinutes(15),
                        SeatHoldStatus.HOLD, null, null, null)
        );

        when(service.getActiveHoldsByTrip(1L)).thenReturn(holds);

        mvc.perform(get("/api/v1/seat-holds/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByTripAndUser_shouldReturn200() throws Exception {
        var holds = List.of(
                new SeatHoldResponse(1L, "A1", OffsetDateTime.now().plusMinutes(15),
                        SeatHoldStatus.HOLD, null, null, null)
        );

        when(service.getActiveHoldsByTripAndUser(1L, 1L)).thenReturn(holds);

        mvc.perform(get("/api/v1/seat-holds/trip/1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByUser_shouldReturn200() throws Exception {
        var holds = List.of(
                new SeatHoldResponse(1L, "A1", OffsetDateTime.now().plusMinutes(15),
                        SeatHoldStatus.HOLD, null, null, null)
        );

        when(service.getHoldsByUser(1L)).thenReturn(holds);

        mvc.perform(get("/api/v1/seat-holds/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkSeatOnHold_shouldReturn200() throws Exception {
        when(service.isSeatOnHold(1L, "A1")).thenReturn(true);

        mvc.perform(get("/api/v1/seat-holds/check")
                        .param("tripId", "1")
                        .param("seatNumber", "A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void getExpirationTime_shouldReturn200() throws Exception {
        var expirationTime = OffsetDateTime.now().plusMinutes(15);
        when(service.calculateExpirationTime()).thenReturn(expirationTime);

        mvc.perform(get("/api/v1/seat-holds/expiration-time"))
                .andExpect(status().isOk());
    }
}
