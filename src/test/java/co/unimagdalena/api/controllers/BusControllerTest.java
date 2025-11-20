package co.unimagdalena.api.controllers;

import co.unimagdalena.api.BusController;
import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.services.BusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusController.class)
@Import(TestSecurityConfig.class)
class BusControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean BusService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new BusCreateRequest("ABC123", 40, BusStatus.AVAILABLE, Set.of());
        var resp = new BusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE, Set.of(), List.of(), List.of());

        when(service.createBus(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/buses/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC123"))
                .andExpect(jsonPath("$.capacity").value(40));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new BusUpdateRequest(45, BusStatus.IN_SERVICE, "ABC123", Set.of());
        var resp = new BusResponse(1L, "ABC123", 45, BusStatus.IN_SERVICE, Set.of(), List.of(), List.of());

        when(service.updateBus(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/buses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(45))
                .andExpect(jsonPath("$.status").value("IN_SERVICE"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/buses/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteBus(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new BusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE, Set.of(), List.of(), List.of());

        when(service.getBusById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/buses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }

    @Test
    void getByPlate_shouldReturn200() throws Exception {
        var resp = new BusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE, Set.of(), List.of(), List.of());

        when(service.getBusByLicensePlate("ABC123")).thenReturn(resp);

        mvc.perform(get("/api/v1/buses/plate/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }

    @Test
    void getByStatus_shouldReturn200() throws Exception {
        var buses = List.of(
                new BusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE, Set.of(), List.of(), List.of()),
                new BusResponse(2L, "XYZ789", 45, BusStatus.AVAILABLE, Set.of(), List.of(), List.of())
        );

        when(service.getBusesByStatus(BusStatus.AVAILABLE)).thenReturn(buses);

        mvc.perform(get("/api/v1/buses/status/AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByCapacity_shouldReturn200() throws Exception {
        var buses = List.of(
                new BusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE, Set.of(), List.of(), List.of())
        );

        when(service.findBusesByCapacity(40)).thenReturn(buses);

        mvc.perform(get("/api/v1/buses/capacity/40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateStatus_shouldReturn204() throws Exception {
        mvc.perform(patch("/api/v1/buses/1/status")
                        .param("status", "IN_MAINTENANCE"))
                .andExpect(status().isNoContent());

        verify(service).updateBusStatus(1L, BusStatus.IN_MAINTENANCE);
    }
}
