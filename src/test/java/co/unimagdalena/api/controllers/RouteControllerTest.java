package co.unimagdalena.api.controllers;

import co.unimagdalena.api.RouteController;
import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.api.dto.StopDto;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.services.RouteService;
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

@WebMvcTest(RouteController.class)
@Import(TestSecurityConfig.class)
class RouteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean RouteService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new RouteCreateRequest("R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                400.0f, 480.0f);
        var resp = new RouteResponse(1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                400.0f, 480.0f, List.of(), List.of());

        when(service.createRoute(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/routes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("R001"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new RouteUpdateRequest("R001", "Bogotá - Medellín Express", "Bogotá",
                "Medellín", 400.0f, 450.0f);

        mvc.perform(patch("/api/v1/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateRoute(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/routes/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteRoute(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new RouteResponse(1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                400.0f, 480.0f, List.of(), List.of());

        when(service.getRouteById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("R001"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                        400.0f, 480.0f, List.of(), List.of()),
                new RouteResponse(2L, "R002", "Medellín - Cali", "Medellín", "Cali",
                        300.0f, 360.0f, List.of(), List.of())
        );

        when(service.getAllRoutes()).thenReturn(routes);

        mvc.perform(get("/api/v1/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_shouldReturn200() throws Exception {
        var routes = List.of(
                new RouteResponse(1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                        400.0f, 480.0f, List.of(), List.of())
        );

        when(service.searchRoutes("Bogotá", "Medellín")).thenReturn(routes);

        mvc.perform(get("/api/v1/routes/search")
                        .param("origin", "Bogotá")
                        .param("destination", "Medellín"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getStops_shouldReturn200() throws Exception {
        var stops = List.of(
                new StopDto.StopResponse(1L, "Terminal Bogotá", 1, 4.7110, -74.0721),
                new StopDto.StopResponse(2L, "Terminal Medellín", 2, 6.2442, -75.5812)
        );

        when(service.getStopsByRouteId(1L)).thenReturn(stops);

        mvc.perform(get("/api/v1/routes/1/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addStop_shouldReturn200() throws Exception {
        var resp = new RouteResponse(1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                400.0f, 480.0f, List.of(), List.of());

        when(service.addStopToRoute(1L, 1L, 1)).thenReturn(resp);

        mvc.perform(post("/api/v1/routes/1/stops/1")
                        .param("stopOrder", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void removeStop_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/routes/1/stops/1"))
                .andExpect(status().isNoContent());

        verify(service).removeStopFromRoute(1L, 1L);
    }

    @Test
    void reorderStops_shouldReturn200() throws Exception {
        var stopOrders = List.of(
                new RouteService.StopOrderRequest(1L, 1),
                new RouteService.StopOrderRequest(2L, 2)
        );
        var resp = new RouteResponse(1L, "R001", "Bogotá - Medellín", "Bogotá", "Medellín",
                400.0f, 480.0f, List.of(), List.of());

        when(service.reorderStops(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/routes/1/stops/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(stopOrders)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
