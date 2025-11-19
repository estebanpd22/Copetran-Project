package co.unimagdalena.services;

import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.SeatRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.BusServiceImpl;
import co.unimagdalena.services.mapper.BusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusServiceImplTest {

    @Mock
    private BusRepository busRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BusMapper busMapper;

    @InjectMocks
    private BusServiceImpl busService;

    private Bus bus;
    private BusCreateRequest createRequest;
    private BusUpdateRequest updateRequest;
    private BusResponse busResponse;

    @BeforeEach
    void setUp() {
        bus = createBus();
        createRequest = createBusCreateRequest();
        updateRequest = createBusUpdateRequest();
        busResponse = createBusResponse();
    }

    // ==================== HELPER METHODS ====================
    private Bus createBus() {
        return Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .soatExpirationDate(OffsetDateTime.now().plusMonths(6))
                .trips(new ArrayList<>())
                .seats(new ArrayList<>())
                .build();
    }

    private BusCreateRequest createBusCreateRequest() {
        return new BusCreateRequest("ABC123", 40, BusStatus.AVAILABLE, null);
    }

    private BusUpdateRequest createBusUpdateRequest() {
        return new BusUpdateRequest(40, BusStatus.IN_SERVICE, "ABC123", null);
    }

    private BusResponse createBusResponse() {
        return new BusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE, null, null, null);
    }

    private Trip createTrip(TripStatus status) {
        return Trip.builder()
                .id(1L)
                .status(status)
                .build();
    }

    // ==================== CREATE BUS TESTS ====================
    @Test
    @DisplayName("Should create bus successfully")
    void shouldCreateBus() {
        when(busRepository.findByPlate(createRequest.plate())).thenReturn(Optional.empty());
        when(busMapper.toEntity(createRequest)).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(busResponse);

        BusResponse result = busService.createBus(createRequest);

        assertNotNull(result);
        verify(busRepository).findByPlate(createRequest.plate());
        verify(busRepository).save(bus);
        verify(seatRepository).saveAll(any());
    }

    @Test
    @DisplayName("Should throw exception when plate already exists")
    void shouldThrowExceptionWhenPlateAlreadyExists() {
        when(busRepository.findByPlate(createRequest.plate())).thenReturn(Optional.of(bus));

        assertThrows(IllegalStateException.class, () -> busService.createBus(createRequest));
        verify(busRepository).findByPlate(createRequest.plate());
        verify(busRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when capacity is not multiple of 4")
    void shouldThrowExceptionWhenCapacityIsNotMultipleOf4() {
        BusCreateRequest invalidRequest = new BusCreateRequest("XYZ789", 41, BusStatus.AVAILABLE, null);

        assertThrows(IllegalArgumentException.class, () -> busService.createBus(invalidRequest));
        verify(busRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create seats for bus with correct capacity")
    void shouldCreateSeatsForBusWithCorrectCapacity() {
        when(busRepository.findByPlate(createRequest.plate())).thenReturn(Optional.empty());
        when(busMapper.toEntity(createRequest)).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(busResponse);

        busService.createBus(createRequest);

        verify(seatRepository).saveAll(argThat(seats -> seatRepository.countSeatsByBus_Id(bus.getId()) == 40));
        }

    // ==================== GET BY ID TESTS ====================
    @Test
    @DisplayName("Should get bus by id successfully")
    void shouldGetBusById() {
        when(busRepository.findById(1L)).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(busResponse);

        BusResponse result = busService.getBusById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(busRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when bus not found by id")
    void shouldThrowExceptionWhenBusNotFoundById() {
        when(busRepository.findById(1L)).thenReturn(bus);

        assertThrows(NotFoundException.class, () -> busService.getBusById(1L));
    }

    // ==================== UPDATE BUS TESTS ====================
    @Test
    @DisplayName("Should update bus successfully")
    void shouldUpdateBus() {
        when(busRepository.findById(1L)).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(busResponse);
        doNothing().when(busMapper).updateEntity(updateRequest, bus);

        BusResponse result = busService.updateBus(1L, updateRequest);

        assertNotNull(result);
        verify(busRepository).findById(1L);
        verify(busMapper).updateEntity(updateRequest, bus);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent bus")
    void shouldThrowExceptionWhenUpdatingNonExistentBus() {
        when(busRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> busService.updateBus(1L, updateRequest));
    }

    // ==================== DELETE BUS TESTS ====================
    @Test
    @DisplayName("Should delete bus successfully when no active trips")
    void shouldDeleteBusSuccessfullyWhenNoActiveTrips() {
        bus.getTrips().add(createTrip(TripStatus.ARRIVED));
        when(busRepository.findById(1L)).thenReturn(bus);

        busService.deleteBus(1L);

        verify(busRepository).findById(1L);
        verify(busRepository).delete(bus);
    }

    @Test
    @DisplayName("Should throw exception when deleting bus with active trips")
    void shouldThrowExceptionWhenDeletingBusWithActiveTrips() {
        bus.getTrips().add(createTrip(TripStatus.SCHEDULED));
        when(busRepository.findById(1L)).thenReturn(bus);

        assertThrows(IllegalArgumentException.class, () -> busService.deleteBus(1L));
        verify(busRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent bus")
    void shouldThrowExceptionWhenDeletingNonExistentBus() {
        when(busRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> busService.deleteBus(1L));
    }

    @Test
    @DisplayName("Should throw exception when bus has BOARDING trip")
    void shouldThrowExceptionWhenBusHasBoardingTrip() {
        bus.getTrips().add(createTrip(TripStatus.BOARDING));
        when(busRepository.findById(1L)).thenReturn(bus);

        assertThrows(IllegalArgumentException.class, () -> busService.deleteBus(1L));
    }

    @Test
    @DisplayName("Should throw exception when bus has DEPARTED trip")
    void shouldThrowExceptionWhenBusHasDepartedTrip() {
        bus.getTrips().add(createTrip(TripStatus.DEPARTED));
        when(busRepository.findById(1L)).thenReturn(bus);

        assertThrows(IllegalArgumentException.class, () -> busService.deleteBus(1L));
    }
}
