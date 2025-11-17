package co.unimagdalena.api.controllers;

import co.unimagdalena.api.AmenityController;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.services.mapper.AmenityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(AmenityController.class)
class AmenityControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean AmenityService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new AmenityCreateRequest("WiFi");
        var resp = new AmenityResponse(1L, "WiFi");

        when(service.createAmenity(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/amenities/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("WiFi"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        when(service.getAmenityById(1L))
                .thenReturn(new AmenityResponse(1L, "Air Conditioning"));

        mvc.perform(get("/api/v1/amenities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Air Conditioning"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var amenities = List.of(
                new AmenityResponse(1L, "WiFi"),
                new AmenityResponse(2L, "Air Conditioning")
        );

        when(service.getAllAmenities()).thenReturn(amenities);

        mvc.perform(get("/api/v1/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new AmenityUpdateRequest("USB Charger");
        var resp = new AmenityResponse(1L, "USB Charger");

        when(service.updateAmenity(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/amenities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("USB Charger"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/amenities/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteAmenity(1L);
    }
}
