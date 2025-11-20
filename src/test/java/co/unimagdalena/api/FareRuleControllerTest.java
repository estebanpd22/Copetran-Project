package co.unimagdalena.api;

import co.unimagdalena.api.FareRuleController;
import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.domine.entities.DynamicPricing;
import co.unimagdalena.services.FareRuleService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FareRuleController.class)
@Import(TestSecurityConfig.class)
class FareRuleControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean FareRuleService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new FareRuleCreateRequest(new BigDecimal("50.00"), DynamicPricing.ON,
                Map.of("STUDENT", 0.15), 1L, 1L, 2L);
        var resp = new FareRuleResponse(1L, new BigDecimal("50.00"), DynamicPricing.ON,
                Map.of("STUDENT", 0.15), null, null, null);

        when(service.createFareRule(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/fareRules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/fareRules/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.basePrice").value(50.00));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new FareRuleUpdateRequest(new BigDecimal("55.00"), DynamicPricing.OFF,
                Map.of("SENIOR", 0.20), 1L, 1L, 2L);

        mvc.perform(patch("/api/v1/fareRules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateFareRule(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/fareRules/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteFareRule(1L);
    }

    @Test
    void get_shouldReturn200() throws Exception {
        var resp = new FareRuleResponse(1L, new BigDecimal("50.00"), DynamicPricing.ON,
                Map.of("STUDENT", 0.15), null, null, null);

        when(service.getFareRule(1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/fareRules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.basePrice").value(50.00));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var fareRules = List.of(
                new FareRuleResponse(1L, new BigDecimal("50.00"), DynamicPricing.ON, Map.of(), null, null, null),
                new FareRuleResponse(2L, new BigDecimal("60.00"), DynamicPricing.OFF, Map.of(), null, null, null)
        );

        when(service.getAllFareRules()).thenReturn(fareRules);

        mvc.perform(get("/api/v1/fareRules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByRoute_shouldReturn200() throws Exception {
        var fareRules = List.of(
                new FareRuleResponse(1L, new BigDecimal("50.00"), DynamicPricing.ON, Map.of(), null, null, null)
        );

        when(service.getFareRulesByRouteId(1L)).thenReturn(fareRules);

        mvc.perform(get("/api/v1/fareRules/route/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void calculatePrice_shouldReturn200() throws Exception {
        when(service.getFinalTicketPrice(1L, 1L, 2L, 1L, 1L, "A1", 1L))
                .thenReturn(new BigDecimal("42.50"));

        mvc.perform(get("/api/v1/fareRules/calculate-price")
                        .param("routeId", "1")
                        .param("fromStopId", "1")
                        .param("toStopId", "2")
                        .param("passengerId", "1")
                        .param("busId", "1")
                        .param("seatNumber", "A1")
                        .param("tripId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(42.50));
    }
}
