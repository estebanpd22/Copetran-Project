package co.unimagdalena.services;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.BusRepository;
import co.unimagdalena.domine.repositories.SeatRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.RouteRepository;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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

    // ===== Helper Methods =====

    private BusCreateRequest createBusCreateRequest(String plate, Integer capacity, BusStatus status, Set<AmenityCreateRequest> amenities) {
        return new BusCreateRequest(plate, capacity, status, amenities);
    }

    private BusUpdateRequest createBusUpdateRequest(Integer capacity, BusStatus status, String plate, Set<AmenityUpdateRequest> amenities) {
        return new BusUpdateRequest(capacity, status, plate, amenities);
    }

    private Bus createBus(Long id, String plate, Integer capacity, BusStatus status, Set<Amenity> amenities, List<Trip> trips) {
        Bus bus = Bus.builder()
                .id(id)
                .plate(plate)
                .capacity(capacity)
                .status(status)
                .amenities(amenities != null ? amenities : new HashSet<>())
                .soatExpirationDate(OffsetDateTime.now().plusMonths(6))
                .trips(trips != null ? trips : new ArrayList<>())
                .build();
        return bus;
    }

    private BusResponse createBusResponse(Long id, String plate, Integer capacity, BusStatus status) {
        return new BusResponse(id, plate, capacity, status, new HashSet<>(), new ArrayList<>(), new ArrayList<>());
    }

    private Trip createTrip(Long id, TripStatus status) {
        return Trip.builder()
                .id(id)
                .status(status)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now())
                .arrivalAt(OffsetDateTime.now().plusHours(3))
                .build();
    }

    // ===== Test createBus =====

    @Test
    @DisplayName("Debe crear un bus exitosamente con capacidad múltiplo de 4")
    void shouldCreateBusSuccessfully() {
        // Given
        String plate = "ABC123";
        Integer capacity = 40;
        BusStatus status = BusStatus.AVAILABLE;
        Set<AmenityCreateRequest> amenities = Set.of(new AmenityCreateRequest("WiFi"), new AmenityCreateRequest("AC"));

        BusCreateRequest request = createBusCreateRequest(plate, capacity, status, amenities);
        Bus bus = createBus(1L, plate, capacity, status, new HashSet<>(), new ArrayList<>());
        BusResponse response = createBusResponse(1L, plate, capacity, status);

        when(busRepository.findByPlate(plate)).thenReturn(Optional.empty());
        when(busMapper.toEntity(request)).thenReturn(bus);
        when(busRepository.save(any(Bus.class))).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(response);
        when(seatRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        BusResponse result = busService.createBus(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.plate()).isEqualTo(plate);
        assertThat(result.capacity()).isEqualTo(capacity);
        assertThat(result.status()).isEqualTo(status);

        verify(busRepository, times(1)).findByPlate(plate);
        verify(busRepository, times(1)).save(any(Bus.class));
        verify(seatRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la placa ya existe")
    void shouldThrowExceptionWhenPlateAlreadyExists() {
        // Given
        String plate = "ABC123";
        BusCreateRequest request = createBusCreateRequest(plate, 40, BusStatus.AVAILABLE, null);
        Bus existingBus = createBus(1L, plate, 40, BusStatus.AVAILABLE, null, null);

        when(busRepository.findByPlate(plate)).thenReturn(Optional.of(existingBus));

        // When & Then
        assertThatThrownBy(() -> busService.createBus(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Bus with plate '" + plate + "' already exists.");

        verify(busRepository, times(1)).findByPlate(plate);
        verify(busRepository, never()).save(any(Bus.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la capacidad no es múltiplo de 4")
    void shouldThrowExceptionWhenCapacityIsNotMultipleOf4() {
        // Given
        String plate = "ABC123";
        Integer invalidCapacity = 37; // No es múltiplo de 4
        BusCreateRequest request = createBusCreateRequest(plate, invalidCapacity, BusStatus.AVAILABLE, null);

        when(busRepository.findByPlate(plate)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.createBus(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capacity must be a multiple of 4");

        verify(busRepository, times(1)).findByPlate(plate);
        verify(busRepository, never()).save(any(Bus.class));
    }

    @Test
    @DisplayName("Debe crear bus sin amenities cuando no se proporcionan")
    void shouldCreateBusWithoutAmenities() {
        // Given
        String plate = "XYZ789";
        Integer capacity = 40;
        BusCreateRequest request = createBusCreateRequest(plate, capacity, BusStatus.AVAILABLE, null);
        Bus bus = createBus(1L, plate, capacity, BusStatus.AVAILABLE, new HashSet<>(), new ArrayList<>());
        BusResponse response = createBusResponse(1L, plate, capacity, BusStatus.AVAILABLE);

        when(busRepository.findByPlate(plate)).thenReturn(Optional.empty());
        when(busMapper.toEntity(request)).thenReturn(bus);
        when(busRepository.save(any(Bus.class))).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(response);
        when(seatRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        BusResponse result = busService.createBus(request);

        // Then
        assertThat(result).isNotNull();
        verify(busRepository, times(1)).save(any(Bus.class));
        verify(seatRepository, times(1)).saveAll(anyList());
    }

    // ===== Test getBusById =====

    @Test
    @DisplayName("Debe obtener un bus por ID exitosamente")
    void shouldGetBusByIdSuccessfully() {
        // Given
        Long busId = 1L;
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE, null, null);
        BusResponse response = createBusResponse(busId, "ABC123", 40, BusStatus.AVAILABLE);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
        when(busMapper.toResponse(bus)).thenReturn(response);

        // When
        BusResponse result = busService.getBusById(busId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(busId);
        verify(busRepository, times(1)).findById(busId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el bus no existe")
    void shouldThrowExceptionWhenBusNotFound() {
        // Given
        Long busId = 999L;
        when(busRepository.findById(busId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.getBusById(busId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus with ID " + busId + " not found");

        verify(busRepository, times(1)).findById(busId);
    }

    // ===== Test updateBus =====

    @Test
    @DisplayName("Debe actualizar un bus exitosamente")
    void shouldUpdateBusSuccessfully() {
        // Given
        Long busId = 1L;
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE, new HashSet<>(), new ArrayList<>());
        BusUpdateRequest updateRequest = createBusUpdateRequest(44, BusStatus.IN_SERVICE, "ABC123", null);
        BusResponse response = createBusResponse(busId, "ABC123", 44, BusStatus.IN_SERVICE);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
        doNothing().when(busMapper).updateEntity(updateRequest, bus);
        when(busMapper.toResponse(bus)).thenReturn(response);

        // When
        BusResponse result = busService.updateBus(busId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(busId);
        verify(busRepository, times(1)).findById(busId);
        verify(busMapper, times(1)).updateEntity(updateRequest, bus);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar un bus que no existe")
    void shouldThrowExceptionWhenUpdatingNonExistentBus() {
        // Given
        Long busId = 999L;
        BusUpdateRequest updateRequest = createBusUpdateRequest(44, BusStatus.IN_SERVICE, null, null);

        when(busRepository.findById(busId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.updateBus(busId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus with ID " + busId + " not found");

        verify(busRepository, times(1)).findById(busId);
        verify(busMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("Debe actualizar bus con nuevas amenities")
    void shouldUpdateBusWithNewAmenities() {
        // Given
        Long busId = 1L;
        Set<AmenityUpdateRequest> newAmenities = Set.of(new AmenityUpdateRequest("GPS"));
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE, new HashSet<>(), new ArrayList<>());
        BusUpdateRequest updateRequest = createBusUpdateRequest(40, BusStatus.AVAILABLE, "ABC123", newAmenities);
        BusResponse response = createBusResponse(busId, "ABC123", 40, BusStatus.AVAILABLE);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
        doNothing().when(busMapper).updateEntity(updateRequest, bus);
        when(busMapper.toResponse(bus)).thenReturn(response);

        // When
        BusResponse result = busService.updateBus(busId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(busRepository, times(1)).findById(busId);
    }

    // ===== Test deleteBus =====

    @Test
    @DisplayName("Debe marcar bus como fuera de servicio cuando no tiene viajes activos")
    void shouldMarkBusAsOutOfServiceWhenNoActiveTrips() {
        // Given
        Long busId = 1L;
        Trip completedTrip = createTrip(1L, TripStatus.ARRIVED);
        List<Trip> trips = List.of(completedTrip);
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE, null, trips);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
        when(busRepository.save(bus)).thenReturn(bus);

        // When
        busService.deleteBus(busId);

        // Then
        assertThat(bus.getStatus()).isEqualTo(BusStatus.OUT_OF_SERVICE);
        verify(busRepository, times(1)).findById(busId);
        verify(busRepository, times(1)).save(bus);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el bus tiene viajes activos programados")
    void shouldThrowExceptionWhenBusHasActiveScheduledTrips() {
        // Given
        Long busId = 1L;
        Trip activeTrip = createTrip(1L, TripStatus.SCHEDULED);
        List<Trip> trips = List.of(activeTrip);
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.ASSIGNED, null, trips);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // When & Then
        assertThatThrownBy(() -> busService.deleteBus(busId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete bus: It is assigned to an active trip");

        verify(busRepository, times(1)).findById(busId);
        verify(busRepository, never()).save(any(Bus.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el bus tiene viajes en estado BOARDING")
    void shouldThrowExceptionWhenBusHasBoardingTrips() {
        // Given
        Long busId = 1L;
        Trip boardingTrip = createTrip(1L, TripStatus.BOARDING);
        List<Trip> trips = List.of(boardingTrip);
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.ASSIGNED, null, trips);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // When & Then
        assertThatThrownBy(() -> busService.deleteBus(busId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete bus: It is assigned to an active trip");

        verify(busRepository, times(1)).findById(busId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el bus tiene viajes en estado DEPARTED")
    void shouldThrowExceptionWhenBusHasDepartedTrips() {
        // Given
        Long busId = 1L;
        Trip departedTrip = createTrip(1L, TripStatus.DEPARTED);
        List<Trip> trips = List.of(departedTrip);
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.IN_SERVICE, null, trips);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));

        // When & Then
        assertThatThrownBy(() -> busService.deleteBus(busId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete bus: It is assigned to an active trip");

        verify(busRepository, times(1)).findById(busId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar un bus que no existe")
    void shouldThrowExceptionWhenDeletingNonExistentBus() {
        // Given
        Long busId = 999L;
        when(busRepository.findById(busId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.deleteBus(busId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus with ID " + busId + " not found");

        verify(busRepository, times(1)).findById(busId);
    }

    @Test
    @DisplayName("Debe permitir eliminar bus con viajes cancelados")
    void shouldAllowDeleteBusWithCancelledTrips() {
        // Given
        Long busId = 1L;
        Trip cancelledTrip = createTrip(1L, TripStatus.CANCELLED);
        List<Trip> trips = List.of(cancelledTrip);
        Bus bus = createBus(busId, "ABC123", 40, BusStatus.AVAILABLE, null, trips);

        when(busRepository.findById(busId)).thenReturn(Optional.of(bus));
        when(busRepository.save(bus)).thenReturn(bus);

        // When
        busService.deleteBus(busId);

        // Then
        assertThat(bus.getStatus()).isEqualTo(BusStatus.OUT_OF_SERVICE);
        verify(busRepository, times(1)).save(bus);
    }

    // ===== Test updateBusStatus =====

    @Test
    @DisplayName("Debe actualizar el estado del bus exitosamente")
    void shouldUpdateBusStatusSuccessfully() {
        // Given
        Long busId = 1L;
        BusStatus newStatus = BusStatus.IN_MAINTENANCE;

        when(busRepository.existsById(busId)).thenReturn(true);
        doNothing().when(busRepository).changeBusStatus(busId, newStatus);

        // When
        busService.updateBusStatus(busId, newStatus);

        // Then
        verify(busRepository, times(1)).existsById(busId);
        verify(busRepository, times(1)).changeBusStatus(busId, newStatus);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar estado de bus inexistente")
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistentBus() {
        // Given
        Long busId = 999L;
        BusStatus newStatus = BusStatus.IN_MAINTENANCE;

        when(busRepository.existsById(busId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> busService.updateBusStatus(busId, newStatus))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus with ID " + busId + " not found");

        verify(busRepository, times(1)).existsById(busId);
        verify(busRepository, never()).changeBusStatus(anyLong(), any(BusStatus.class));
    }

    // ===== Test getBusesByStatus =====

    @Test
    @DisplayName("Debe obtener buses por estado exitosamente")
    void shouldGetBusesByStatusSuccessfully() {
        // Given
        BusStatus status = BusStatus.AVAILABLE;
        Bus bus1 = createBus(1L, "ABC123", 40, status, null, null);
        Bus bus2 = createBus(2L, "XYZ789", 44, status, null, null);
        List<Bus> buses = List.of(bus1, bus2);

        BusResponse response1 = createBusResponse(1L, "ABC123", 40, status);
        BusResponse response2 = createBusResponse(2L, "XYZ789", 44, status);

        when(busRepository.findBusesByStatus(status)).thenReturn(buses);
        when(busMapper.toResponse(bus1)).thenReturn(response1);
        when(busMapper.toResponse(bus2)).thenReturn(response2);

        // When
        List<BusResponse> result = busService.getBusesByStatus(status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
        verify(busRepository, times(1)).findBusesByStatus(status);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay buses con el estado especificado")
    void shouldReturnEmptyListWhenNoBusesWithStatus() {
        // Given
        BusStatus status = BusStatus.IN_MAINTENANCE;
        when(busRepository.findBusesByStatus(status)).thenReturn(Collections.emptyList());

        // When
        List<BusResponse> result = busService.getBusesByStatus(status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(busRepository, times(1)).findBusesByStatus(status);
    }

    // ===== Test findBusesByCapacity =====

    @Test
    @DisplayName("Debe encontrar buses con capacidad igual o mayor a la requerida")
    void shouldFindBusesByRequiredCapacity() {
        // Given
        int requiredCapacity = 40;
        Bus bus1 = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE, null, null);
        Bus bus2 = createBus(2L, "XYZ789", 44, BusStatus.AVAILABLE, null, null);
        Bus bus3 = createBus(3L, "DEF456", 36, BusStatus.AVAILABLE, null, null); // No cumple

        List<Bus> allBuses = List.of(bus1, bus2, bus3);

        BusResponse response1 = createBusResponse(1L, "ABC123", 40, BusStatus.AVAILABLE);
        BusResponse response2 = createBusResponse(2L, "XYZ789", 44, BusStatus.AVAILABLE);

        when(busRepository.findAll()).thenReturn(allBuses);
        when(busMapper.toResponse(bus1)).thenReturn(response1);
        when(busMapper.toResponse(bus2)).thenReturn(response2);

        // When
        List<BusResponse> result = busService.findBusesByCapacity(requiredCapacity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).capacity()).isGreaterThanOrEqualTo(requiredCapacity);
        assertThat(result.get(1).capacity()).isGreaterThanOrEqualTo(requiredCapacity);
        verify(busRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando ningún bus cumple la capacidad requerida")
    void shouldReturnEmptyListWhenNoBusMatchesCapacity() {
        // Given
        int requiredCapacity = 60;
        Bus bus1 = createBus(1L, "ABC123", 40, BusStatus.AVAILABLE, null, null);
        Bus bus2 = createBus(2L, "XYZ789", 44, BusStatus.AVAILABLE, null, null);

        when(busRepository.findAll()).thenReturn(List.of(bus1, bus2));

        // When
        List<BusResponse> result = busService.findBusesByCapacity(requiredCapacity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(busRepository, times(1)).findAll();
    }

    // ===== Test getBusByLicensePlate =====

    @Test
    @DisplayName("Debe obtener bus por placa exitosamente")
    void shouldGetBusByLicensePlateSuccessfully() {
        // Given
        String plate = "ABC123";
        Bus bus = createBus(1L, plate, 40, BusStatus.AVAILABLE, null, null);
        BusResponse response = createBusResponse(1L, plate, 40, BusStatus.AVAILABLE);

        when(busRepository.findByPlate(plate)).thenReturn(Optional.of(bus));
        when(busMapper.toResponse(bus)).thenReturn(response);

        // When
        BusResponse result = busService.getBusByLicensePlate(plate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.plate()).isEqualTo(plate);
        verify(busRepository, times(1)).findByPlate(plate);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no existe bus con la placa especificada")
    void shouldThrowExceptionWhenBusWithPlateNotFound() {
        // Given
        String plate = "XYZ999";
        when(busRepository.findByPlate(plate)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.getBusByLicensePlate(plate))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus with plate '" + plate + "' not found");

        verify(busRepository, times(1)).findByPlate(plate);
    }
}
