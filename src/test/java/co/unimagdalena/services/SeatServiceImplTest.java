package co.unimagdalena.services;

import co.unimagdalena.api.dto.BusDto;
import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.domine.entities.Bus;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.domine.entities.Seat;
import co.unimagdalena.domine.entities.SeatStatus;
import co.unimagdalena.domine.entities.SeatType;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.SeatRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.SeatServiceImpl;
import co.unimagdalena.services.mapper.SeatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatServiceImpl Tests")
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatMapper seatMapper;

    @Mock
    private BusRepository busRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    // ==================== HELPER METHODS ====================

    private Bus createValidBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .soatExpirationDate(OffsetDateTime.now().plusYears(1))
                .build();
    }

    private Seat createValidSeat() {
        return Seat.builder()
                .id(1L)
                .number(1)
                .price(new BigDecimal("50000"))
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();
    }

    private SeatCreateRequest createValidSeatCreateRequest() {
        return new SeatCreateRequest(
                new BigDecimal("50000"),
                1,
                SeatType.STANDARD,
                SeatStatus.AVAILABLE,
                1L
        );
    }

    private SeatUpdateRequest createValidSeatUpdateRequest() {
        return new SeatUpdateRequest(
                new BigDecimal("55000"),
                1,
                SeatType.PREFERENTIAL,
                SeatStatus.AVAILABLE,
                1L
        );
    }

    private SeatResponse createValidSeatResponse() {
        return new SeatResponse(
                1L,
                new BigDecimal("50000"),
                1,
                SeatType.STANDARD,
                SeatStatus.AVAILABLE,
                new BusDto.BusSummary(1L, "ABC-123", 40, BusStatus.AVAILABLE)
        );
    }

    // ==================== CREATE SEAT TESTS ====================

    @Test
    @DisplayName("Should create a new seat successfully when all validations pass")
    void shouldCreateSeatSuccessfully() {
        // Arrange
        SeatCreateRequest request = createValidSeatCreateRequest();
        Bus bus = createValidBus();
        Seat seatEntity = createValidSeat();
        SeatResponse response = createValidSeatResponse();

        when(busRepository.findById(1L)).thenReturn(bus);
        when(seatRepository.findByBusIdAndNumber(1L, 1)).thenReturn(Optional.empty());
        when(seatRepository.countByBusId(1L)).thenReturn(0L);
        when(seatMapper.toEntity(request)).thenReturn(seatEntity);
        when(seatRepository.save(any(Seat.class))).thenReturn(seatEntity);
        when(seatMapper.toResponse(seatEntity)).thenReturn(response);

        // Act
        SeatResponse result = seatService.createSeat(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1, result.number());
        verify(busRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).findByBusIdAndNumber(1L, 1);
        verify(seatRepository, times(1)).countByBusId(1L);
        verify(seatRepository, times(1)).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when bus does not exist")
    void shouldThrowNotFoundExceptionWhenBusDoesNotExist() {
        // Arrange
        SeatCreateRequest request = createValidSeatCreateRequest();
        when(busRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> seatService.createSeat(request));
        verify(busRepository, times(1)).findById(1L);
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when seat number already exists in bus")
    void shouldThrowExceptionWhenSeatNumberDuplicate() {
        // Arrange
        SeatCreateRequest request = createValidSeatCreateRequest();
        Bus bus = createValidBus();
        Seat existingSeat = createValidSeat();

        when(busRepository.findById(1L)).thenReturn(bus);
        when(seatRepository.findByBusIdAndNumber(1L, 1)).thenReturn(Optional.of(existingSeat));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> seatService.createSeat(request));
        verify(busRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).findByBusIdAndNumber(1L, 1);
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when bus has reached maximum capacity")
    void shouldThrowExceptionWhenBusAtMaxCapacity() {
        // Arrange
        SeatCreateRequest request = createValidSeatCreateRequest();
        Bus bus = createValidBus();

        when(busRepository.findById(1L)).thenReturn(bus);
        when(seatRepository.findByBusIdAndNumber(1L, 1)).thenReturn(Optional.empty());
        when(seatRepository.countByBusId(1L)).thenReturn(40L); // Already at capacity

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> seatService.createSeat(request));
        verify(seatRepository, times(1)).countByBusId(1L);
        verify(seatRepository, never()).save(any(Seat.class));
    }

    // ==================== UPDATE SEAT TESTS ====================

    @Test
    @DisplayName("Should update seat successfully when all validations pass")
    void shouldUpdateSeatSuccessfully() {
        // Arrange
        SeatUpdateRequest request = createValidSeatUpdateRequest();
        Seat existingSeat = createValidSeat();
        Seat updatedSeat = Seat.builder()
                .id(1L)
                .number(1)
                .price(new BigDecimal("55000"))
                .type(SeatType.PREFERENTIAL)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();
        SeatResponse response = new SeatResponse(
                1L,
                new BigDecimal("55000"),
                1,
                SeatType.PREFERENTIAL,
                SeatStatus.AVAILABLE,
                new BusDto.BusSummary(1L, "ABC-123", 40, BusStatus.AVAILABLE)
        );

        when(seatRepository.findById(1L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.save(any(Seat.class))).thenReturn(updatedSeat);
        when(seatMapper.toResponse(updatedSeat)).thenReturn(response);

        // Act
        SeatResponse result = seatService.updateSeat(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("55000"), result.price());
        verify(seatRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when seat does not exist for update")
    void shouldThrowNotFoundExceptionWhenSeatDoesNotExistForUpdate() {
        // Arrange
        SeatUpdateRequest request = createValidSeatUpdateRequest();
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> seatService.updateSeat(1L, request));
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when updating to duplicate seat number")
    void shouldThrowExceptionWhenUpdatingToDuplicateSeatNumber() {
        // Arrange
        SeatUpdateRequest request = new SeatUpdateRequest(
                new BigDecimal("50000"),
                2,  // Different number
                SeatType.STANDARD,
                SeatStatus.AVAILABLE,
                1L
        );
        Seat existingSeat = createValidSeat();
        Seat anotherSeat = Seat.builder()
                .id(2L)
                .number(2)
                .price(new BigDecimal("50000"))
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();

        when(seatRepository.findById(1L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.findByBusIdAndNumber(1L, 2)).thenReturn(Optional.of(anotherSeat));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> seatService.updateSeat(1L, request));
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should update seat without duplicate check when number is unchanged")
    void shouldUpdateSeatWhenNumberIsUnchanged() {
        // Arrange
        SeatUpdateRequest request = new SeatUpdateRequest(
                new BigDecimal("55000"),
                1,  // Same number
                SeatType.PREFERENTIAL,
                SeatStatus.AVAILABLE,
                1L
        );
        Seat existingSeat = createValidSeat();
        Seat updatedSeat = Seat.builder()
                .id(1L)
                .number(1)
                .price(new BigDecimal("55000"))
                .type(SeatType.PREFERENTIAL)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();
        SeatResponse response = new SeatResponse(
                1L,
                new BigDecimal("55000"),
                1,
                SeatType.PREFERENTIAL,
                SeatStatus.AVAILABLE,
                new BusDto.BusSummary(1L, "ABC-123", 40, BusStatus.AVAILABLE)
        );

        when(seatRepository.findById(1L)).thenReturn(Optional.of(existingSeat));
        when(seatRepository.save(any(Seat.class))).thenReturn(updatedSeat);
        when(seatMapper.toResponse(updatedSeat)).thenReturn(response);

        // Act
        SeatResponse result = seatService.updateSeat(1L, request);

        // Assert
        assertNotNull(result);
        verify(seatRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).save(any(Seat.class));
    }

    // ==================== DELETE SEAT TESTS ====================

    @Test
    @DisplayName("Should delete seat successfully")
    void shouldDeleteSeatSuccessfully() {
        // Arrange
        Seat seat = createValidSeat();
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Act
        seatService.deleteSeat(1L);

        // Assert
        verify(seatRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).delete(seat);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent seat")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentSeat() {
        // Arrange
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> seatService.deleteSeat(1L));
        verify(seatRepository, never()).delete(any(Seat.class));
    }

    // ==================== GET SEAT BY ID TESTS ====================

    @Test
    @DisplayName("Should retrieve seat by ID successfully")
    void shouldGetSeatByIdSuccessfully() {
        // Arrange
        Seat seat = createValidSeat();
        SeatResponse response = createValidSeatResponse();

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatMapper.toResponse(seat)).thenReturn(response);

        // Act
        SeatResponse result = seatService.getSeatById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(seatRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting non-existent seat by ID")
    void shouldThrowNotFoundExceptionWhenGettingSeatByInvalidId() {
        // Arrange
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> seatService.getSeatById(999L));
    }

    // ==================== GET SEATS BY BUS ID AND TYPE TESTS ====================

    @Test
    @DisplayName("Should retrieve seats by bus ID and type successfully")
    void shouldGetSeatsByBusIdAndTypeSuccessfully() {
        // Arrange
        Seat seat1 = createValidSeat();
        Seat seat2 = Seat.builder()
                .id(2L)
                .number(2)
                .price(new BigDecimal("50000"))
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();

        SeatResponse response1 = createValidSeatResponse();
        SeatResponse response2 = new SeatResponse(
                2L,
                new BigDecimal("50000"),
                2,
                SeatType.STANDARD,
                SeatStatus.AVAILABLE,
                new BusDto.BusSummary(1L, "ABC-123", 40, BusStatus.AVAILABLE)
        );

        when(seatRepository.findByBusIdAndType(1L, SeatType.STANDARD))
                .thenReturn(List.of(seat1, seat2));
        when(seatMapper.toResponse(seat1)).thenReturn(response1);
        when(seatMapper.toResponse(seat2)).thenReturn(response2);

        // Act
        List<SeatResponse> result = seatService.getSeatsByBusIdAndType(1L, "STANDARD");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(seatRepository, times(1)).findByBusIdAndType(1L, SeatType.STANDARD);
    }

    @Test
    @DisplayName("Should return empty list when no seats match bus ID and type")
    void shouldReturnEmptyListWhenNoSeatsMatchCriteria() {
        // Arrange
        when(seatRepository.findByBusIdAndType(1L, SeatType.PREFERENTIAL))
                .thenReturn(List.of());

        // Act
        List<SeatResponse> result = seatService.getSeatsByBusIdAndType(1L, "PREFERENTIAL");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(seatRepository, times(1)).findByBusIdAndType(1L, SeatType.PREFERENTIAL);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when seat type is invalid")
    void shouldThrowExceptionWhenSeatTypeIsInvalid() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                seatService.getSeatsByBusIdAndType(1L, "INVALID_TYPE")
        );
        verify(seatRepository, never()).findByBusIdAndType(anyLong(), any());
    }

    @Test
    @DisplayName("Should convert lowercase seat type to uppercase")
    void shouldConvertLowercaseSeatTypeToUppercase() {
        // Arrange
        Seat seat = createValidSeat();
        SeatResponse response = createValidSeatResponse();

        when(seatRepository.findByBusIdAndType(1L, SeatType.STANDARD))
                .thenReturn(List.of(seat));
        when(seatMapper.toResponse(seat)).thenReturn(response);

        // Act
        List<SeatResponse> result = seatService.getSeatsByBusIdAndType(1L, "standard");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(seatRepository, times(1)).findByBusIdAndType(1L, SeatType.STANDARD);
    }

    // ==================== GET SEATS BY FEATURE TESTS ====================

    @Test
    @DisplayName("Should retrieve seats by valid SeatType feature")
    void shouldGetSeatsByValidSeatTypeFeature() {
        // Arrange
        Seat seat = createValidSeat();
        SeatResponse response = createValidSeatResponse();

        when(seatRepository.findAll()).thenReturn(List.of(seat));
        when(seatMapper.toResponse(seat)).thenReturn(response);

        // Act
        List<SeatResponse> result = seatService.getSeatsByFeature("STANDARD");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(seatRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve window seats by VENTANA feature")
    void shouldGetWindowSeatsByVentanaFeature() {
        // Arrange
        Seat oddNumberedSeat = Seat.builder()
                .id(1L)
                .number(1) // Odd = window
                .price(new BigDecimal("50000"))
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();

        Seat evenNumberedSeat = Seat.builder()
                .id(2L)
                .number(2) // Even = aisle
                .price(new BigDecimal("50000"))
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(createValidBus())
                .build();

        SeatResponse response1 = createValidSeatResponse();

        when(seatRepository.findAll()).thenReturn(List.of(oddNumberedSeat, evenNumberedSeat));
        when(seatMapper.toResponse(oddNumberedSeat)).thenReturn(response1);

        // Act
        List<SeatResponse> result = seatService.getSeatsByFeature("VENTANA");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(seatRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when unrecognized feature is requested")
    void shouldReturnEmptyListForUnrecognizedFeature() {
        // Arrange
        when(seatRepository.findAll()).thenReturn(List.of());

        // Act
        List<SeatResponse> result = seatService.getSeatsByFeature("UNKNOWN_FEATURE");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(seatRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should convert lowercase feature to uppercase for SeatType matching")
    void shouldConvertLowercaseFeatureToUppercase() {
        // Arrange
        Seat seat = createValidSeat();
        SeatResponse response = createValidSeatResponse();

        when(seatRepository.findAll()).thenReturn(List.of(seat));
        when(seatMapper.toResponse(seat)).thenReturn(response);

        // Act
        List<SeatResponse> result = seatService.getSeatsByFeature("standard");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(seatRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no seats match SeatType feature")
    void shouldReturnEmptyListWhenNoSeatsMatchSeatTypeFeature() {
        // Arrange
        Seat seat = createValidSeat();
        when(seatRepository.findAll()).thenReturn(List.of(seat));

        // Act - searching for PREFERENTIAL but only STANDARD seats exist
        List<SeatResponse> result = seatService.getSeatsByFeature("PREFERENTIAL");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(seatRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when SeatRepository returns no seats")
    void shouldReturnEmptyListWhenRepositoryReturnsNoSeats() {
        // Arrange
        when(seatRepository.findAll()).thenReturn(List.of());

        // Act
        List<SeatResponse> result = seatService.getSeatsByFeature("STANDARD");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(seatRepository, times(1)).findAll();
    }
}