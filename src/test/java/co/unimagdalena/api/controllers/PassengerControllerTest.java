package co.unimagdalena.api.controllers;

import co.unimagdalena.api.PassengerController;
import co.unimagdalena.api.dto.PassengerDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.services.PassengerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PassengerController.class)
@Import(TestSecurityConfig.class)
class PassengerControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean PassengerService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new PassengerCreateRequest("John Doe", "CC", "123456789",
                LocalDate.of(1990, 1, 1), "1234567890", null);
        var resp = new PassengerResponse(1L, "John Doe", "CC", "123456789",
                LocalDate.of(1990, 1, 1), "1234567890");

        when(service.createPassenger(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/passengers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/passengers/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new PassengerUpdateRequest(1L, "John Updated", "CC", "123456789",
                LocalDate.of(1990, 1, 1), "1234567890");

        mvc.perform(patch("/api/v1/passengers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updatePassenger(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/passengers/1"))
                .andExpect(status().isNoContent());

        verify(service).deletePassenger(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new PassengerResponse(1L, "John Doe", "CC", "123456789",
                LocalDate.of(1990, 1, 1), "1234567890");

        when(service.getPassengerById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/passengers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void getByDocument_shouldReturn200() throws Exception {
        var resp = new PassengerResponse(1L, "John Doe", "CC", "123456789",
                LocalDate.of(1990, 1, 1), "1234567890");

        when(service.finByDocumentNumber("123456789")).thenReturn(resp);

        mvc.perform(get("/api/v1/passengers/document/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("123456789"));
    }

    @Test
    void getByUser_shouldReturn200() throws Exception {
        var passengers = List.of(
                new PassengerResponse(1L, "John Doe", "CC", "123456789", LocalDate.of(1990, 1, 1), "1234567890"),
                new PassengerResponse(2L, "Jane Doe", "CC", "987654321", LocalDate.of(1992, 5, 15), "0987654321")
        );

        when(service.getPassengerByUser(1L)).thenReturn(passengers);

        mvc.perform(get("/api/v1/passengers/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
