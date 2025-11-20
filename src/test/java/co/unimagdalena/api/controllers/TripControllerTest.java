package co.unimagdalena.api.controllers;

import co.unimagdalena.api.TripController;
import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.TripStatus;
import co.unimagdalena.services.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@Import(TestSecurityConfig.class)
class TripControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean TripService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new TripCreateRequest(LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(4), TripStatus.SCHEDULED, 1L, 1L);
        var resp = new TripResponse(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(4), TripStatus.SCHEDULED, null, null);

        when(service.createTrip(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/trips/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new TripUpdateRequest(LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(4), TripStatus.BOARDING, 1L, 1L);

        mvc.perform(patch("/api/v1/trips/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateTrip(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/trips/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteTrip(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new TripResponse(1L, LocalDate.now(), OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(4), TripStatus.SCHEDULED, null, null);

        when(service.getTripDetails(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void search_shouldReturn200() throws Exception {
        var trips = List.of(
                new TripResponse(1L, LocalDate.now(), OffsetDateTime.now(),
                        OffsetDateTime.now().plusHours(4), TripStatus.SCHEDULED, null, null),
                new TripResponse(2L, LocalDate.now(), OffsetDateTime.now().plusHours(6),
                        OffsetDateTime.now().plusHours(10), TripStatus.SCHEDULED, null, null)
        );

        when(service.getTrips("Bogotá", "Medellín", LocalDate.now())).thenReturn(trips);

        mvc.perform(get("/api/v1/trips/search")
                        .param("origin", "Bogotá")
                        .param("destination", "Medellín")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateStatus_shouldReturn204() throws Exception {
        mvc.perform(patch("/api/v1/trips/1/status")
                        .param("status", "DEPARTED"))
                .andExpect(status().isNoContent());

        verify(service).updateTripStatus(1L, TripStatus.DEPARTED);
    }

    @Test
    void getStatistics_shouldReturn200() throws Exception {
        when(service.getTripStatistics(1L)).thenReturn(35L);

        mvc.perform(get("/api/v1/trips/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(35));
    }

    @Test
    void checkOverbooking_shouldReturn200() throws Exception {
        when(service.checkOverbookingConditions(1L)).thenReturn(false);

        mvc.perform(get("/api/v1/trips/1/check-overbooking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
