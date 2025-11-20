package co.unimagdalena.api.controllers;

import co.unimagdalena.api.StopController;
import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.services.StopService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StopController.class)
@Import(TestSecurityConfig.class)
class StopControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean StopService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new StopCreateRequest("Terminal Bogotá", 1, 4.7110, -74.0721, 1L);
        var resp = new StopResponse(1L, "Terminal Bogotá", 1, 4.7110, -74.0721);

        when(service.createStop(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/stops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/stops/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Terminal Bogotá"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new StopUpdateRequest("Terminal Norte Bogotá", 1, 4.7110, -74.0721, 1L);
        var resp = new StopResponse(1L, "Terminal Norte Bogotá", 1, 4.7110, -74.0721);

        when(service.updateStop(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/stops/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Terminal Norte Bogotá"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/stops/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteStop(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new StopResponse(1L, "Terminal Bogotá", 1, 4.7110, -74.0721);

        when(service.getStopById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/stops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Terminal Bogotá"));
    }

    @Test
    void getAllActive_shouldReturn200() throws Exception {
        var stops = List.of(
                new StopResponse(1L, "Terminal Bogotá", 1, 4.7110, -74.0721),
                new StopResponse(2L, "Terminal Medellín", 2, 6.2442, -75.5812)
        );

        when(service.getAllActiveStops()).thenReturn(stops);

        mvc.perform(get("/api/v1/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByCity_shouldReturn200() throws Exception {
        var stops = List.of(
                new StopResponse(1L, "Terminal Bogotá", 1, 4.7110, -74.0721)
        );

        when(service.getStopsByCity("Bogotá")).thenReturn(stops);

        mvc.perform(get("/api/v1/stops/city/Bogotá"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getNearLocation_shouldReturn200() throws Exception {
        var stops = List.of(
                new StopResponse(1L, "Terminal Bogotá", 1, 4.7110, -74.0721),
                new StopResponse(2L, "Terminal Norte", 2, 4.7150, -74.0750)
        );

        when(service.findStopsNearLocation(4.7110, -74.0721, 10.0)).thenReturn(stops);

        mvc.perform(get("/api/v1/stops/near")
                        .param("latitude", "4.7110")
                        .param("longitude", "-74.0721")
                        .param("radiusKm", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
