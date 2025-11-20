package co.unimagdalena.api.controllers;

import co.unimagdalena.api.TicketController;
import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.TicketStatus;
import co.unimagdalena.services.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@Import(TestSecurityConfig.class)
class TicketControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean TicketService service;

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new TicketResponse(1L, new BigDecimal("50.00"),
                co.unimagdalena.domine.entities.PaymentMethod.CARD, TicketStatus.SOLD,
                OffsetDateTime.now(), "QR123", null, null, null, null, null);

        when(service.getTicket(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.qrCode").value("QR123"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/tickets/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteTicket(1L);
    }

    @Test
    void getByTrip_shouldReturn200() throws Exception {
        var tickets = List.of(
                new TicketResponse(1L, new BigDecimal("50.00"),
                        co.unimagdalena.domine.entities.PaymentMethod.CARD, TicketStatus.SOLD,
                        OffsetDateTime.now(), "QR123", null, null, null, null, null),
                new TicketResponse(2L, new BigDecimal("50.00"),
                        co.unimagdalena.domine.entities.PaymentMethod.CASH, TicketStatus.SOLD,
                        OffsetDateTime.now(), "QR456", null, null, null, null, null)
        );

        when(service.getTicketsByTrip(1L)).thenReturn(tickets);

        mvc.perform(get("/api/v1/tickets/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByPurchase_shouldReturn200() throws Exception {
        var tickets = List.of(
                new TicketResponse(1L, new BigDecimal("50.00"),
                        co.unimagdalena.domine.entities.PaymentMethod.CARD, TicketStatus.SOLD,
                        OffsetDateTime.now(), "QR123", null, null, null, null, null)
        );

        when(service.getTicketsByPurchase(1L)).thenReturn(tickets);

        mvc.perform(get("/api/v1/tickets/purchase/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByPassenger_shouldReturn200() throws Exception {
        var tickets = List.of(
                new TicketResponse(1L, new BigDecimal("50.00"),
                        co.unimagdalena.domine.entities.PaymentMethod.CARD, TicketStatus.SOLD,
                        OffsetDateTime.now(), "QR123", null, null, null, null, null)
        );

        when(service.getTicketsByPassenger(1L)).thenReturn(tickets);

        mvc.perform(get("/api/v1/tickets/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void generateQr_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/tickets/1/generate-qr"))
                .andExpect(status().isNoContent());

        verify(service).generateQrForTicket(1L);
    }

    @Test
    void validateQr_shouldReturn200() throws Exception {
        mvc.perform(post("/api/v1/tickets/validate-qr")
                        .param("qrCode", "QR123"))
                .andExpect(status().isOk());

        verify(service).validateQrForTicket("QR123");
    }
}
