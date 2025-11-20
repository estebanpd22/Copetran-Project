package co.unimagdalena.api;

import co.unimagdalena.api.BaggageController;
import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.api.dto.TicketDto;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.services.BaggageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(BaggageController.class)
@Import(TestSecurityConfig.class)
class BaggageControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean BaggageService service;

    @Test
    void update_shouldReturn200() throws Exception {
        var req = new BaggageUpdateRequest(15.5f, new BigDecimal("25.00"), "TAG123", 1L);
        var ticketSummary = new TicketDto.TicketSummary(1L, new BigDecimal("50.00"),
                co.unimagdalena.domine.entities.TicketStatus.SOLD, "QR123", null);
        var resp = new BaggageResponse(1L, 15.5f, new BigDecimal("25.00"), "TAG123", ticketSummary);

        when(service.updateBaggage(eq(1L), any())).thenReturn(resp);

        mvc.perform(patch("/api/v1/baggages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(15.5))
                .andExpect(jsonPath("$.tagCode").value("TAG123"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/baggages/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteBaggage(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var ticketSummary = new TicketDto.TicketSummary(1L, new BigDecimal("50.00"),
                co.unimagdalena.domine.entities.TicketStatus.SOLD, "QR123", null);
        var resp = new BaggageResponse(1L, 15.5f, new BigDecimal("25.00"), "TAG123", ticketSummary);

        when(service.getBaggageById(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/baggages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.weightKg").value(15.5));
    }

    @Test
    void getByTag_shouldReturn200() throws Exception {
        var ticketSummary = new TicketDto.TicketSummary(1L, new BigDecimal("50.00"),
                co.unimagdalena.domine.entities.TicketStatus.SOLD, "QR123", null);
        var resp = new BaggageResponse(1L, 15.5f, new BigDecimal("25.00"), "TAG123", ticketSummary);

        when(service.getBaggageByTagCode("TAG123")).thenReturn(resp);

        mvc.perform(get("/api/v1/baggages/tag/TAG123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagCode").value("TAG123"));
    }

    @Test
    void getByTicket_shouldReturn200() throws Exception {
        var baggages = List.of(
                new BaggageResponse(1L, 15.5f, new BigDecimal("25.00"), "TAG123", null),
                new BaggageResponse(2L, 20.0f, new BigDecimal("30.00"), "TAG456", null)
        );

        when(service.getBaggageByTicketId(1L)).thenReturn(baggages);

        mvc.perform(get("/api/v1/baggages/ticket/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void assignTag_shouldReturn204() throws Exception {
        mvc.perform(post("/api/v1/baggages/1/assign-tag")
                        .param("tagCode", "TAG123"))
                .andExpect(status().isNoContent());

        verify(service).assignTagCode(1L, "TAG123");
    }

    @Test
    void calculateFee_shouldReturn200() throws Exception {
        when(service.calculateFee(15.5)).thenReturn(new BigDecimal("25.00"));

        mvc.perform(get("/api/v1/baggages/calculate-fee")
                        .param("weightKg", "15.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(25.00));
    }
}
