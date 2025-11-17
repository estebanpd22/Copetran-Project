package co.unimagdalena.api.controllers;

import co.unimagdalena.api.ParcelController;
import co.unimagdalena.api.dto.ParcelDto.*;
import co.unimagdalena.domine.entities.ParcelStatus;
import co.unimagdalena.services.ParcelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ParcelController.class)
class ParcelControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean ParcelService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new ParcelCreateRequest("John Doe", "1234567890", "Jane Smith", "0987654321",
                new BigDecimal("25.00"), ParcelStatus.CREATED, null, "123456", 1L, 2L, 1L);
        var resp = new ParcelResponse(1L, "PCL001", "John Doe", "1234567890", "Jane Smith",
                "0987654321", new BigDecimal("25.00"), ParcelStatus.CREATED, null, "123456", null, null, null);

        when(service.createParcel(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/parcels/PCL001")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("PCL001"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new ParcelUpdateRequest("John Doe", "1234567890", "Jane Smith", "0987654321",
                new BigDecimal("30.00"), ParcelStatus.IN_TRANSIT, null, "123456", 1L, 2L, 1L);

        mvc.perform(patch("/api/v1/parcels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateParcel(eq(1L), any());
    }

    @Test
    void getByCode_shouldReturn200() throws Exception {
        var resp = new ParcelResponse(1L, "PCL001", "John Doe", "1234567890", "Jane Smith",
                "0987654321", new BigDecimal("25.00"), ParcelStatus.CREATED, null, "123456", null, null, null);

        when(service.getParcelByCode("PCL001")).thenReturn(resp);

        mvc.perform(get("/api/v1/parcels/code/PCL001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PCL001"));
    }

    @Test
    void getByTrip_shouldReturn200() throws Exception {
        var parcels = List.of(
                new ParcelResponse(1L, "PCL001", "John Doe", "1234567890", "Jane Smith",
                        "0987654321", new BigDecimal("25.00"), ParcelStatus.IN_TRANSIT, null, "123456", null, null, null)
        );

        when(service.getParcelsByTrip(1L)).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBySender_shouldReturn200() throws Exception {
        var parcels = List.of(
                new ParcelResponse(1L, "PCL001", "John Doe", "1234567890", "Jane Smith",
                        "0987654321", new BigDecimal("25.00"), ParcelStatus.CREATED, null, "123456", null, null, null)
        );

        when(service.getParcelsBySender("1234567890")).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/sender/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByReceiver_shouldReturn200() throws Exception {
        var parcels = List.of(
                new ParcelResponse(1L, "PCL001", "John Doe", "1234567890", "Jane Smith",
                        "0987654321", new BigDecimal("25.00"), ParcelStatus.IN_TRANSIT, null, "123456", null, null, null)
        );

        when(service.getParcelsByReceiver("0987654321")).thenReturn(parcels);

        mvc.perform(get("/api/v1/parcels/receiver/0987654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void assignTrip_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/parcels/1/assign-trip")
                        .param("tripId", "1"))
                .andExpect(status().isNoContent());

        verify(service).assignTrip(1L, 1L);
    }

    @Test
    void confirmDelivery_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/parcels/1/confirm-delivery")
                        .param("otp", "123456")
                        .param("proofPhotoUrl", "http://example.com/photo.jpg"))
                .andExpect(status().isNoContent());

        verify(service).confirmDelivery(1L, "123456", "http://example.com/photo.jpg");
    }

    @Test
    void markFailed_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/parcels/1/mark-failed")
                        .param("failureNote", "Receiver not available"))
                .andExpect(status().isNoContent());

        verify(service).markDeliveryFailed(1L, "Receiver not available");
    }
}
