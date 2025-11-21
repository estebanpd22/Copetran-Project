package co.unimagdalena.services;

import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.domine.entities.Baggage;
import co.unimagdalena.domine.entities.Ticket;
import co.unimagdalena.domine.repositories.BaggageRepository;
import co.unimagdalena.domine.repositories.TicketRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.BaggageServiceImpl;
import co.unimagdalena.services.mapper.BaggageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaggageServiceImplTest {

    @Mock
    private BaggageRepository baggageRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BaggageMapper mapper;

    @InjectMocks
    private BaggageServiceImpl baggageService;

    private Baggage baggage;
    private Ticket ticket;
    private BaggageCreateRequest createRequest;
    private BaggageUpdateRequest updateRequest;
    private BaggageResponse baggageResponse;

    @BeforeEach
    void setUp() {
        ticket = createTicket();
        baggage = createBaggage();
        createRequest = createBaggageCreateRequest();
        updateRequest = createBaggageUpdateRequest();
        baggageResponse = createBaggageResponse();
    }

    // ==================== HELPER METHODS ====================
    private Ticket createTicket() {
        return Ticket.builder()
                .id(1L)
                .build();
    }

    private Baggage createBaggage() {
        return Baggage.builder()
                .id(1L)
                .weightKg(15.0f)
                .fee(BigDecimal.ZERO)
                .tagCode("BAG001")
                .ticket(ticket)
                .build();
    }

    private BaggageCreateRequest createBaggageCreateRequest() {
        return new BaggageCreateRequest(15.0f, BigDecimal.ZERO, "BAG001", 1L);
    }

    private BaggageUpdateRequest createBaggageUpdateRequest() {
        return new BaggageUpdateRequest(25.0f, BigDecimal.valueOf(7.5), "BAG002", 1L);
    }

    private BaggageResponse createBaggageResponse() {
        return new BaggageResponse(1L, 15.0f, BigDecimal.ZERO, "BAG001", null);
    }

    // ==================== CREATE BAGGAGE TESTS ====================
    @Test
    @DisplayName("Should create baggage successfully")
    void shouldCreateBaggage() {
        when(mapper.toEntity(createRequest)).thenReturn(baggage);

        Baggage result = baggageService.createBaggage(createRequest, ticket);

        assertNotNull(result);
        assertEquals(ticket, result.getTicket());
        verify(baggageRepository).save(baggage);
    }

    @Test
    @DisplayName("Should throw exception when ticket is null")
    void shouldThrowExceptionWhenTicketIsNull() {
        assertThrows(IllegalArgumentException.class, () -> baggageService.createBaggage(createRequest, null));
        verify(baggageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when weight is zero or negative")
    void shouldThrowExceptionWhenWeightIsZeroOrNegative() {
        BaggageCreateRequest invalidRequest = new BaggageCreateRequest(0f, BigDecimal.ZERO, "BAG001", 1L);

        assertThrows(IllegalArgumentException.class, () -> baggageService.createBaggage(invalidRequest, ticket));
        verify(baggageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate fee when weight exceeds 20kg")
    void shouldCalculateFeeWhenWeightExceeds20Kg() {
        BaggageCreateRequest heavyRequest = new BaggageCreateRequest(25.0f, BigDecimal.ZERO, "BAG001", 1L);
        Baggage heavyBaggage = Baggage.builder()
                .weightKg(25.0f)
                .build();

        when(mapper.toEntity(heavyRequest)).thenReturn(heavyBaggage);

        Baggage result = baggageService.createBaggage(heavyRequest, ticket);

        assertNotNull(result.getFee());
        assertTrue(result.getFee().compareTo(BigDecimal.ZERO) > 0);
        verify(baggageRepository).save(any());
    }

    // ==================== UPDATE BAGGAGE TESTS ====================
    @Test
    @DisplayName("Should update baggage successfully")
    void shouldUpdateBaggage() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(mapper.toResponse(baggage)).thenReturn(baggageResponse);
        doNothing().when(mapper).updateEntity(updateRequest, baggage);

        BaggageResponse result = baggageService.updateBaggage(1L, updateRequest);

        assertNotNull(result);
        verify(baggageRepository).findById(1L);
        verify(mapper).updateEntity(updateRequest, baggage);
        verify(baggageRepository).save(baggage);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent baggage")
    void shouldThrowExceptionWhenUpdatingNonExistentBaggage() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> baggageService.updateBaggage(1L, updateRequest));
        verify(baggageRepository).findById(1L);
        verify(baggageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should recalculate fee when weight is updated")
    void shouldRecalculateFeeWhenWeightIsUpdated() {
        BaggageUpdateRequest weightUpdate = new BaggageUpdateRequest(30.0f, null, null, null);
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(mapper.toResponse(baggage)).thenReturn(baggageResponse);
        doNothing().when(mapper).updateEntity(weightUpdate, baggage);

        baggageService.updateBaggage(1L, weightUpdate);

        verify(baggageRepository).save(baggage);
        assertNotNull(baggage.getFee());
    }

    // ==================== DELETE BAGGAGE TESTS ====================
    @Test
    @DisplayName("Should delete baggage successfully")
    void shouldDeleteBaggage() {
        when(baggageRepository.existsById(1L)).thenReturn(true);

        baggageService.deleteBaggage(1L);

        verify(baggageRepository).existsById(1L);
        verify(baggageRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent baggage")
    void shouldThrowExceptionWhenDeletingNonExistentBaggage() {
        when(baggageRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> baggageService.deleteBaggage(1L));
        verify(baggageRepository).existsById(1L);
        verify(baggageRepository, never()).deleteById(any());
    }

    // ==================== CALCULATE FEE TESTS ====================
    @Test
    @DisplayName("Should return zero fee for weight under 20kg")
    void shouldReturnZeroFeeForWeightUnder20Kg() {
        BigDecimal fee = baggageService.calculateFee(15.0);

        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    @DisplayName("Should calculate fee for weight over 20kg")
    void shouldCalculateFeeForWeightOver20Kg() {
        BigDecimal fee = baggageService.calculateFee(25.0);

        assertTrue(fee.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should throw exception when weight is negative or zero")
    void shouldThrowExceptionWhenWeightIsNegativeOrZero() {
        assertThrows(IllegalArgumentException.class, () -> baggageService.calculateFee(-5.0));
        assertThrows(IllegalArgumentException.class, () -> baggageService.calculateFee(0.0));
    }

    // ==================== ASSIGN TAG CODE TESTS ====================
    @Test
    @DisplayName("Should assign tag code successfully")
    void shouldAssignTagCode() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(baggageRepository.existsByTagCode("NEW_TAG")).thenReturn(false);

        baggageService.assignTagCode(1L, "NEW_TAG");

        assertEquals("NEW_TAG", baggage.getTagCode());
        verify(baggageRepository).save(baggage);
    }

    @Test
    @DisplayName("Should throw exception when baggage not found for tag assignment")
    void shouldThrowExceptionWhenBaggageNotFoundForTagAssignment() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> baggageService.assignTagCode(1L, "NEW_TAG"));
        verify(baggageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when tag code is empty")
    void shouldThrowExceptionWhenTagCodeIsEmpty() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));

        assertThrows(IllegalArgumentException.class, () -> baggageService.assignTagCode(1L, ""));
        verify(baggageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when tag code already exists")
    void shouldThrowExceptionWhenTagCodeAlreadyExists() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(baggageRepository.existsByTagCode("EXISTING_TAG")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> baggageService.assignTagCode(1L, "EXISTING_TAG"));
        verify(baggageRepository, never()).save(any());
    }

    // ==================== GET BY ID TESTS ====================
    @Test
    @DisplayName("Should get baggage by id successfully")
    void shouldGetBaggageById() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(mapper.toResponse(baggage)).thenReturn(baggageResponse);

        BaggageResponse result = baggageService.getBaggageById(1L);

        assertNotNull(result);
        verify(baggageRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when baggage not found by id")
    void shouldThrowExceptionWhenBaggageNotFoundById() {
        when(baggageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> baggageService.getBaggageById(1L));
    }

    // ==================== GET BY TAG CODE TESTS ====================
    @Test
    @DisplayName("Should get baggage by tag code successfully")
    void shouldGetBaggageByTagCode() {
        when(baggageRepository.findByTagCode("BAG001")).thenReturn(Optional.of(baggage));
        when(mapper.toResponse(baggage)).thenReturn(baggageResponse);

        BaggageResponse result = baggageService.getBaggageByTagCode("BAG001");

        assertNotNull(result);
        verify(baggageRepository).findByTagCode("BAG001");
    }

    @Test
    @DisplayName("Should throw exception when tag code is empty")
    void shouldThrowExceptionWhenTagCodeIsEmptyForSearch() {
        assertThrows(IllegalArgumentException.class, () -> baggageService.getBaggageByTagCode(""));
    }

    @Test
    @DisplayName("Should throw exception when baggage not found by tag code")
    void shouldThrowExceptionWhenBaggageNotFoundByTagCode() {
        when(baggageRepository.findByTagCode("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> baggageService.getBaggageByTagCode("NONEXISTENT"));
    }

    // ==================== GET BY TICKET TESTS ====================
    @Test
    @DisplayName("Should get baggage by ticket id successfully")
    void shouldGetBaggageByTicketId() {
        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(baggageRepository.findByTicketId(1L)).thenReturn(List.of(baggage));
        when(mapper.toResponse(baggage)).thenReturn(baggageResponse);

        List<BaggageResponse> result = baggageService.getBaggageByTicketId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).existsById(1L);
    }

    @Test
    @DisplayName("Should throw exception when ticket not found")
    void shouldThrowExceptionWhenTicketNotFound() {
        when(ticketRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> baggageService.getBaggageByTicketId(1L));
    }

    @Test
    @DisplayName("Should return empty list when no baggage for ticket")
    void shouldReturnEmptyListWhenNoBaggageForTicket() {
        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(baggageRepository.findByTicketId(1L)).thenReturn(Collections.emptyList());

        List<BaggageResponse> result = baggageService.getBaggageByTicketId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
