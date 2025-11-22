package co.unimagdalena.api;

import co.unimagdalena.api.BaggageController;
import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.api.dto.TicketDto;
import co.unimagdalena.domine.entities.TicketStatus;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.BaggageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BaggageController.class)
@DisplayName("BaggageControllerTest")
class BaggageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BaggageService baggageService;

    private BaggageUpdateRequest updateRequest;
    private BaggageResponse baggageResponse;

    @BeforeEach
    void setUp() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(5);
        updateRequest = new BaggageUpdateRequest(25.5F, new BigDecimal("7.50"), "Tag123",1L);
        baggageResponse = new BaggageResponse(
                1L,
                25.5F,
                new BigDecimal("7.50"),
                "TAG123",
                new TicketDto.TicketSummary(2L,new BigDecimal("7.0"), TicketStatus.NO_SHOW, "qrcode", createdAt)
        );
    }

    // =====================================================================
    // UPDATE BAGGAGE - PATCH /api/v1/baggages/{id}
    // =====================================================================

    @Test
    @DisplayName("update_WithValidData_ReturnsOk200")
    void update_WithValidData_ReturnsOk200() throws Exception {
        Long baggageId = 1L;

        when(baggageService.updateBaggage(baggageId, updateRequest))
                .thenReturn(baggageResponse);

        mockMvc.perform(patch("/api/v1/baggages/{id}", baggageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.weightKg", is(25.5)))
                .andExpect(jsonPath("$.fee", is(7.50)));

        verify(baggageService, times(1)).updateBaggage(baggageId, updateRequest);
    }

    @Test
    @DisplayName("update_WithInvalidId_Returns404NotFound")
    void update_WithInvalidId_Returns404NotFound() throws Exception {
        Long baggageId = 999L;

        when(baggageService.updateBaggage(baggageId, updateRequest))
                .thenThrow(new NotFoundException("Baggage not found"));

        mockMvc.perform(patch("/api/v1/baggages/{id}", baggageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(baggageService, times(1)).updateBaggage(baggageId, updateRequest);
    }

    @Test
    @DisplayName("update_WithNegativeWeight_Returns422UnprocessableEntity")
    void update_WithNegativeWeight_Returns422UnprocessableEntity() throws Exception {
        Long baggageId = 1L;
        BaggageUpdateRequest invalidRequest = new BaggageUpdateRequest(-5.0F, new BigDecimal("7.5"),"TAG123",1L);

        when(baggageService.updateBaggage(baggageId, invalidRequest))
                .thenThrow(new IllegalArgumentException("Weight must be greater than zero"));

        mockMvc.perform(patch("/api/v1/baggages/{id}", baggageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());

        verify(baggageService, times(1)).updateBaggage(baggageId, invalidRequest);
    }

    // =====================================================================
    // DELETE BAGGAGE - DELETE /api/v1/baggages/{id}
    // =====================================================================

    @Test
    @DisplayName("delete_WithValidId_ReturnsNoContent204")
    void delete_WithValidId_ReturnsNoContent204() throws Exception {
        Long baggageId = 1L;

        doNothing().when(baggageService).deleteBaggage(baggageId);

        mockMvc.perform(delete("/api/v1/baggages/{id}", baggageId))
                .andExpect(status().isNoContent());

        verify(baggageService, times(1)).deleteBaggage(baggageId);
    }

    @Test
    @DisplayName("delete_WithInvalidId_Returns404NotFound")
    void delete_WithInvalidId_Returns404NotFound() throws Exception {
        Long baggageId = 999L;

        doThrow(new NotFoundException("Baggage not found"))
                .when(baggageService).deleteBaggage(baggageId);

        mockMvc.perform(delete("/api/v1/baggages/{id}", baggageId))
                .andExpect(status().isNotFound());

        verify(baggageService, times(1)).deleteBaggage(baggageId);
    }

    // =====================================================================
    // GET BAGGAGE BY ID - GET /api/v1/baggages/{id}
    // =====================================================================

    @Test
    @DisplayName("get_WithValidId_ReturnsOk200")
    void get_WithValidId_ReturnsOk200() throws Exception {
        Long baggageId = 1L;

        when(baggageService.getBaggageById(baggageId))
                .thenReturn(baggageResponse);

        mockMvc.perform(get("/api/v1/baggages/{id}", baggageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.weightKg", is(25.5)))
                .andExpect(jsonPath("$.tagCode", is("TAG123")));

        verify(baggageService, times(1)).getBaggageById(baggageId);
    }

    @Test
    @DisplayName("get_WithInvalidId_Returns404NotFound")
    void get_WithInvalidId_Returns404NotFound() throws Exception {
        Long baggageId = 999L;

        when(baggageService.getBaggageById(baggageId))
                .thenThrow(new NotFoundException("Baggage not found"));

        mockMvc.perform(get("/api/v1/baggages/{id}", baggageId))
                .andExpect(status().isNotFound());

        verify(baggageService, times(1)).getBaggageById(baggageId);
    }

    // =====================================================================
    // GET BAGGAGE BY TAG - GET /api/v1/baggages/tag/{tagCode}
    // =====================================================================

    @Test
    @DisplayName("getByTag_WithValidTag_ReturnsOk200")
    void getByTag_WithValidTag_ReturnsOk200() throws Exception {
        String tagCode = "TAG123";

        when(baggageService.getBaggageByTagCode(tagCode))
                .thenReturn(baggageResponse);

        mockMvc.perform(get("/api/v1/baggages/tag/{tagCode}", tagCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tagCode", is("TAG123")));

        verify(baggageService, times(1)).getBaggageByTagCode(tagCode);
    }

    @Test
    @DisplayName("getByTag_WithInvalidTag_Returns404NotFound")
    void getByTag_WithInvalidTag_Returns404NotFound() throws Exception {
        String tagCode = "INVALID";

        when(baggageService.getBaggageByTagCode(tagCode))
                .thenThrow(new NotFoundException("Baggage not found"));

        mockMvc.perform(get("/api/v1/baggages/tag/{tagCode}", tagCode))
                .andExpect(status().isNotFound());

        verify(baggageService, times(1)).getBaggageByTagCode(tagCode);
    }

    @Test
    @DisplayName("getByTag_WithEmptyTag_Returns400BadRequest")
    void getByTag_WithEmptyTag_Returns400BadRequest() throws Exception {
        String tagCode = "";

        when(baggageService.getBaggageByTagCode(tagCode))
                .thenThrow(new IllegalArgumentException("Tag code is required"));

        mockMvc.perform(get("/api/v1/baggages/tag/{tagCode}", tagCode))
                .andExpect(status().isBadRequest());

        verify(baggageService, times(1)).getBaggageByTagCode(tagCode);
    }

    // =====================================================================
    // GET BAGGAGE BY TICKET - GET /api/v1/baggages/ticket/{ticketId}
    // =====================================================================

    @Test
    @DisplayName("getByTicket_WithValidTicketId_ReturnsOk200")
    void getByTicket_WithValidTicketId_ReturnsOk200() throws Exception {
        Long ticketId = 1L;
        List<BaggageResponse> baggages = List.of(baggageResponse);

        when(baggageService.getBaggageByTicketId(ticketId))
                .thenReturn(baggages);

        mockMvc.perform(get("/api/v1/baggages/ticket/{ticketId}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(baggageService, times(1)).getBaggageByTicketId(ticketId);
    }

    @Test
    @DisplayName("getByTicket_WithNoResults_ReturnsOk200WithEmptyList")
    void getByTicket_WithNoResults_ReturnsOk200WithEmptyList() throws Exception {
        Long ticketId = 1L;

        when(baggageService.getBaggageByTicketId(ticketId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/baggages/ticket/{ticketId}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(baggageService, times(1)).getBaggageByTicketId(ticketId);
    }

    @Test
    @DisplayName("getByTicket_WithInvalidTicketId_Returns404NotFound")
    void getByTicket_WithInvalidTicketId_Returns404NotFound() throws Exception {
        Long ticketId = 999L;

        when(baggageService.getBaggageByTicketId(ticketId))
                .thenThrow(new NotFoundException("Ticket not found"));

        mockMvc.perform(get("/api/v1/baggages/ticket/{ticketId}", ticketId))
                .andExpect(status().isNotFound());

        verify(baggageService, times(1)).getBaggageByTicketId(ticketId);
    }

    // =====================================================================
    // ASSIGN TAG CODE - POST /api/v1/baggages/{id}/assign-tag
    // =====================================================================

    @Test
    @DisplayName("assignTag_WithValidData_ReturnsNoContent204")
    void assignTag_WithValidData_ReturnsNoContent204() throws Exception {
        Long baggageId = 1L;
        String tagCode = "NEW_TAG";

        doNothing().when(baggageService).assignTagCode(baggageId, tagCode);

        mockMvc.perform(post("/api/v1/baggages/{id}/assign-tag", baggageId)
                        .param("tagCode", tagCode))
                .andExpect(status().isNoContent());

        verify(baggageService, times(1)).assignTagCode(baggageId, tagCode);
    }

    @Test
    @DisplayName("assignTag_WithInvalidBaggageId_Returns404NotFound")
    void assignTag_WithInvalidBaggageId_Returns404NotFound() throws Exception {
        Long baggageId = 999L;
        String tagCode = "NEW_TAG";

        doThrow(new NotFoundException("Baggage not found"))
                .when(baggageService).assignTagCode(baggageId, tagCode);

        mockMvc.perform(post("/api/v1/baggages/{id}/assign-tag", baggageId)
                        .param("tagCode", tagCode))
                .andExpect(status().isNotFound());

        verify(baggageService, times(1)).assignTagCode(baggageId, tagCode);
    }

    @Test
    @DisplayName("assignTag_WithDuplicateTag_Returns409Conflict")
    void assignTag_WithDuplicateTag_Returns409Conflict() throws Exception {
        Long baggageId = 1L;
        String tagCode = "EXISTING_TAG";

        doThrow(new IllegalStateException("Tag code already in use"))
                .when(baggageService).assignTagCode(baggageId, tagCode);

        mockMvc.perform(post("/api/v1/baggages/{id}/assign-tag", baggageId)
                        .param("tagCode", tagCode))
                .andExpect(status().isConflict());

        verify(baggageService, times(1)).assignTagCode(baggageId, tagCode);
    }

    @Test
    @DisplayName("assignTag_WithEmptyTag_Returns422UnprocessableEntity")
    void assignTag_WithEmptyTag_Returns422UnprocessableEntity() throws Exception {
        Long baggageId = 1L;
        String tagCode = "";

        doThrow(new IllegalArgumentException("Tag code cannot be empty"))
                .when(baggageService).assignTagCode(baggageId, tagCode);

        mockMvc.perform(post("/api/v1/baggages/{id}/assign-tag", baggageId)
                        .param("tagCode", tagCode))
                .andExpect(status().isUnprocessableEntity());

        verify(baggageService, times(1)).assignTagCode(baggageId, tagCode);
    }

    // =====================================================================
    // CALCULATE FEE - GET /api/v1/baggages/calculate-fee
    // =====================================================================

    @Test
    @DisplayName("calculateFee_WithValidWeight_ReturnsOk200")
    void calculateFee_WithValidWeight_ReturnsOk200() throws Exception {
        Double weightKg = 25.5;
        BigDecimal expectedFee = new BigDecimal("8.25");

        when(baggageService.calculateFee(weightKg))
                .thenReturn(expectedFee);

        mockMvc.perform(get("/api/v1/baggages/calculate-fee")
                        .param("weightKg", String.valueOf(weightKg)))
                .andExpect(status().isOk())
                .andExpect(content().string("8.25"));

        verify(baggageService, times(1)).calculateFee(weightKg);
    }

    @Test
    @DisplayName("calculateFee_WithZeroWeight_Returns400BadRequest")
    void calculateFee_WithZeroWeight_Returns400BadRequest() throws Exception {
        Double weightKg = 0.0;

        when(baggageService.calculateFee(weightKg))
                .thenThrow(new IllegalArgumentException("Weight must be positive"));

        mockMvc.perform(get("/api/v1/baggages/calculate-fee")
                        .param("weightKg", String.valueOf(weightKg)))
                .andExpect(status().isBadRequest());

        verify(baggageService, times(1)).calculateFee(weightKg);
    }

    @Test
    @DisplayName("calculateFee_WithNegativeWeight_Returns400BadRequest")
    void calculateFee_WithNegativeWeight_Returns400BadRequest() throws Exception {
        Double weightKg = -10.0;

        when(baggageService.calculateFee(weightKg))
                .thenThrow(new IllegalArgumentException("Weight must be positive"));

        mockMvc.perform(get("/api/v1/baggages/calculate-fee")
                        .param("weightKg", String.valueOf(weightKg)))
                .andExpect(status().isBadRequest());

        verify(baggageService, times(1)).calculateFee(weightKg);
    }

    @Test
    @DisplayName("calculateFee_WithWeightUnder20kg_ReturnsZero")
    void calculateFee_WithWeightUnder20kg_ReturnsZero() throws Exception {
        Double weightKg = 15.0;
        BigDecimal expectedFee = BigDecimal.ZERO;

        when(baggageService.calculateFee(weightKg))
                .thenReturn(expectedFee);

        mockMvc.perform(get("/api/v1/baggages/calculate-fee")
                        .param("weightKg", String.valueOf(weightKg)))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(baggageService, times(1)).calculateFee(weightKg);
    }
}
