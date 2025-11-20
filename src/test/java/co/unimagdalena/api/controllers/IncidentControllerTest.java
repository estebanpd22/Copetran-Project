package co.unimagdalena.api.controllers;

import co.unimagdalena.api.IncidentController;
import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.EntityType;
import co.unimagdalena.domine.entities.IncidentType;
import co.unimagdalena.services.IncidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@Import(TestSecurityConfig.class)
class IncidentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean IncidentService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new IncidentCreateRequest(EntityType.TRIP, 1L, IncidentType.VEHICLE, "Engine failure");
        var resp = new IncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.VEHICLE,
                "Engine failure", LocalTime.now());

        when(service.createIncident(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/incidents/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.incidentType").value("VEHICLE"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new IncidentUpdateRequest(EntityType.TRIP, 1L, IncidentType.VEHICLE, "Engine repaired");

        mvc.perform(patch("/api/v1/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateIncident(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/incidents/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteIncident(1L);
    }

    @Test
    void resolve_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/incidents/1/resolve"))
                .andExpect(status().isNoContent());

        verify(service).resolveIncident(1L);
    }

    @Test
    void getActiveByTrip_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.VEHICLE, "Engine failure", LocalTime.now())
        );

        when(service.getActiveIncidentsByTrip(1L)).thenReturn(incidents);

        mvc.perform(get("/api/v1/incidents/trip/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTripDelay_shouldReturn200() throws Exception {
        when(service.getTotalDelayMinutes(1L)).thenReturn(45L);

        mvc.perform(get("/api/v1/incidents/trip/1/delay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(45));
    }

    @Test
    void getByEntityType_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.VEHICLE, "Engine failure", LocalTime.now())
        );

        when(service.findByIncidentByEntityType(EntityType.TRIP)).thenReturn(incidents);

        mvc.perform(get("/api/v1/incidents/entity-type/TRIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRecentByType_shouldReturn200() throws Exception {
        var incidents = List.of(
                new IncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Security issue", LocalTime.now())
        );

        when(service.findIncidentsRecentByType(IncidentType.SECURITY)).thenReturn(incidents);

        mvc.perform(get("/api/v1/incidents/type/SECURITY/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
