package co.unimagdalena.api.controllers;

import co.unimagdalena.api.PurchaseController;
import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.domine.entities.PaymentMethod;
import co.unimagdalena.domine.entities.PaymentStatus;
import co.unimagdalena.services.PurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
class PurchaseControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean PurchaseService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var ticketReq = new PurchaseCreateRequest.TicketRequest(1L, 1L, 1L, "A1", 1L, 2L, null);
        var req = new PurchaseCreateRequest(1L, PaymentMethod.CARD, List.of(ticketReq));
        var resp = new PurchaseResponse(1L, new BigDecimal("50.00"), PaymentMethod.CARD,
                PaymentStatus.PENDING, OffsetDateTime.now(), null, List.of());

        when(service.createPurchase(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/purchases/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount").value(50.00));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new PurchaseResponse(1L, new BigDecimal("50.00"), PaymentMethod.CARD,
                PaymentStatus.CONFIRMED, OffsetDateTime.now(), null, List.of());

        when(service.getPurchase(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/purchases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("CONFIRMED"));
    }

    @Test
    void getByUser_shouldReturn200() throws Exception {
        var purchases = List.of(
                new PurchaseResponse(1L, new BigDecimal("50.00"), PaymentMethod.CARD,
                        PaymentStatus.CONFIRMED, OffsetDateTime.now(), null, List.of()),
                new PurchaseResponse(2L, new BigDecimal("75.00"), PaymentMethod.CASH,
                        PaymentStatus.PENDING, OffsetDateTime.now(), null, List.of())
        );

        when(service.getPurchasesByUserId(1L)).thenReturn(purchases);

        mvc.perform(get("/api/v1/purchases/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByDateRange_shouldReturn200() throws Exception {
        var purchases = List.of(
                new PurchaseResponse(1L, new BigDecimal("50.00"), PaymentMethod.CARD,
                        PaymentStatus.CONFIRMED, OffsetDateTime.now(), null, List.of())
        );

        when(service.getPurchasesByDateRange(any(), any())).thenReturn(purchases);

        mvc.perform(get("/api/v1/purchases/date-range")
                        .param("start", OffsetDateTime.now().minusDays(7).toString())
                        .param("end", OffsetDateTime.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void confirm_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/purchases/1/confirm")
                        .param("paymentReference", "REF12345"))
                .andExpect(status().isNoContent());

        verify(service).confirmPurchase(1L, "REF12345");
    }

    @Test
    void cancel_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/purchases/1/cancel"))
                .andExpect(status().isNoContent());

        verify(service).cancelPurchase(1L);
    }
}
