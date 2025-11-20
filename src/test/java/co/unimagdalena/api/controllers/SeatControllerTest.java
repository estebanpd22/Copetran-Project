package co.unimagdalena.api.controllers;

import co.unimagdalena.api.SeatController;
import co.unimagdalena.api.dto.BusDto;
import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.domine.entities.SeatStatus;
import co.unimagdalena.domine.entities.SeatType;
import co.unimagdalena.services.SeatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
@Import(TestSecurityConfig.class)
class SeatControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean SeatService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new SeatCreateRequest(new BigDecimal("50.00"), 1, SeatType.STANDARD,
                SeatStatus.AVAILABLE, 1L);
        var busSummary = new BusDto.BusSummary(1L, "ABC123", 40, BusStatus.AVAILABLE);
        var resp = new SeatResponse(1L, new BigDecimal("50.00"), 1, SeatType.STANDARD,
                SeatStatus.AVAILABLE, busSummary);

        when(service.createSeat(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/seats/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new SeatUpdateRequest(new BigDecimal("55.00"), 1, SeatType.PREFERENTIAL,
                SeatStatus.UNAVAILABLE, 1L);
        var busSummary = new BusDto.BusSummary(1L, "ABC123", 40, BusStatus.AVAILABLE);
        var resp = new SeatResponse(1L, new BigDecimal("55.00"), 1, SeatType.PREFERENTIAL,
                SeatStatus.UNAVAILABLE, busSummary);

        when(service.updateSeat(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/seats/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PREFERENTIAL"))
                .andExpect(jsonPath("$.price").value(55.00));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/seats/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteSeat(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var busSummary = new BusDto.BusSummary(1L, "ABC123", 40, BusStatus.AVAILABLE);
        var resp = new SeatResponse(1L, new BigDecimal("50.00"), 1, SeatType.STANDARD,
                SeatStatus.AVAILABLE, busSummary);

        when(service.getSeatById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/seats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void getByBusAndType_shouldReturn200() throws Exception {
        var busSummary = new BusDto.BusSummary(1L, "ABC123", 40, BusStatus.AVAILABLE);
        var seats = List.of(
                new SeatResponse(1L, new BigDecimal("50.00"), 1, SeatType.STANDARD,
                        SeatStatus.AVAILABLE, busSummary),
                new SeatResponse(2L, new BigDecimal("50.00"), 2, SeatType.STANDARD,
                        SeatStatus.AVAILABLE, busSummary)
        );

        when(service.getSeatsByBusIdAndType(1L, "STANDARD")).thenReturn(seats);

        mvc.perform(get("/api/v1/seats/bus/1/type/STANDARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByFeature_shouldReturn200() throws Exception {
        var seats = List.of(
                new SeatResponse(1L, new BigDecimal("60.00"), 1, SeatType.PREFERENTIAL,
                        SeatStatus.AVAILABLE, null)
        );

        when(service.getSeatsByFeature("reclining")).thenReturn(seats);

        mvc.perform(get("/api/v1/seats/feature/reclining"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
