package co.unimagdalena.services;

import co.unimagdalena.api.dto.IncidentDto.IncidentCreateRequest;
import co.unimagdalena.api.dto.ParcelDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.ParcelRepository;
import co.unimagdalena.domine.repositories.StopRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.ParcelServiceImpl;
import co.unimagdalena.services.mapper.ParcelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private IncidentService incidentService;

    @Mock
    private ParcelMapper parcelMapper;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    // ========== Helper methods ==========

    private Stop createStop(Long id, String name, Integer order, Long routeId) {
        Route route = Route.builder()
                .id(routeId)
                .code("R001")
                .name("Ruta Principal")
                .origin("Ciudad A")
                .destination("Ciudad B")
                .distanceKm(100f)
                .durationMin(120f)
                .build();

        return Stop.builder()
                .id(id)
                .name(name)
                .order(order)
                .latitude(10.0)
                .longitude(-75.0)
                .route(route)
                .build();
    }

    private Trip createTrip(Long id, Long routeId, TripStatus status) {
        Route route = Route.builder()
                .id(routeId)
                .code("R001")
                .name("Ruta Principal")
                .build();

        return Trip.builder()
                .id(id)
                .date(LocalDate.now())
                .departureAt(OffsetDateTime.now().plusHours(2))
                .arrivalAt(OffsetDateTime.now().plusHours(4))
                .status(status)
                .route(route)
                .build();
    }

    private Parcel createParcel(Long id, String code, ParcelStatus status, Stop fromStop, Stop toStop, Trip trip) {
        return Parcel.builder()
                .id(id)
                .code(code)
                .senderName("Juan Pérez")
                .senderPhone("3001234567")
                .receiverName("María López")
                .receiverPhone("3109876543")
                .price(BigDecimal.valueOf(50000))
                .status(status)
                .deliveryOTP("123456")
                .fromStop(fromStop)
                .toStop(toStop)
                .trip(trip)
                .build();
    }

    private ParcelCreateRequest createParcelRequest(Long fromStopId, Long toStopId, Long tripId) {
        return new ParcelCreateRequest(
                "Juan Pérez",
                "3001234567",
                "María López",
                "3109876543",
                BigDecimal.valueOf(50000),
                ParcelStatus.CREATED,
                null,
                null,
                fromStopId,
                toStopId,
                tripId
        );
    }

    private ParcelUpdateRequest createUpdateRequest(String senderName, String receiverName) {
        return new ParcelUpdateRequest(
                senderName,
                "3001234567",
                receiverName,
                "3109876543",
                BigDecimal.valueOf(60000),
                ParcelStatus.CREATED,
                null,
                null,
                null,
                null,
                null
        );
    }

    private ParcelResponse createParcelResponse(Parcel parcel) {
        return new ParcelResponse(
                parcel.getId(),
                parcel.getCode(),
                parcel.getSenderName(),
                parcel.getSenderPhone(),
                parcel.getReceiverName(),
                parcel.getReceiverPhone(),
                parcel.getPrice(),
                parcel.getStatus(),
                parcel.getProofPhotoUrl(),
                parcel.getDeliveryOTP(),
                null,
                null,
                null
        );
    }

    // ========== Test cases for createParcel ==========

    @Test
    @DisplayName("Should create parcel successfully without trip assignment")
    void shouldCreateParcelSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        ParcelCreateRequest request = createParcelRequest(1L, 2L, null);

        Parcel parcelEntity = createParcel(null, null, ParcelStatus.CREATED, fromStop, toStop, null);
        Parcel savedParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);
        savedParcel.setDeliveryOTP("123456");

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelMapper.toEntity(request)).thenReturn(parcelEntity);
        when(parcelRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(parcelRepository.save(any(Parcel.class))).thenReturn(savedParcel);
        when(parcelMapper.toResponse(savedParcel)).thenReturn(createParcelResponse(savedParcel));

        // When
        ParcelResponse response = parcelService.createParcel(request);

        // Then
        assertNotNull(response);
        assertEquals("PAQ-20250119-0001", response.code());
        assertEquals(ParcelStatus.CREATED, response.status());
        verify(parcelRepository).save(any(Parcel.class));
        verify(tripRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should create parcel with trip assignment successfully")
    void shouldCreateParcelWithTripSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.SCHEDULED);
        ParcelCreateRequest request = createParcelRequest(1L, 2L, 1L);

        Parcel parcelEntity = createParcel(null, null, ParcelStatus.CREATED, fromStop, toStop, null);
        Parcel savedParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        savedParcel.setDeliveryOTP("123456");

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelMapper.toEntity(request)).thenReturn(parcelEntity);
        when(parcelRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(savedParcel);
        when(parcelMapper.toResponse(savedParcel)).thenReturn(createParcelResponse(savedParcel));

        // When
        ParcelResponse response = parcelService.createParcel(request);

        // Then
        assertNotNull(response);
        assertEquals(ParcelStatus.IN_TRANSIT, response.status());
        verify(tripRepository).findById(1L);
        verify(parcelRepository).save(any(Parcel.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when fromStop does not exist")
    void shouldThrowExceptionWhenFromStopNotFound() {
        // Given
        ParcelCreateRequest request = createParcelRequest(999L, 2L, null);
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("La parada de origen no existe"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when toStop does not exist")
    void shouldThrowExceptionWhenToStopNotFound() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        ParcelCreateRequest request = createParcelRequest(1L, 999L, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("La parada de destino no existe"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when stops belong to different routes")
    void shouldThrowExceptionWhenStopsBelongToDifferentRoutes() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 2L); // Different route
        ParcelCreateRequest request = createParcelRequest(1L, 2L, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("deben pertenecer a la misma ruta"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when fromStop order is greater than or equal to toStop order")
    void shouldThrowExceptionWhenInvalidStopOrder() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 5, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        ParcelCreateRequest request = createParcelRequest(1L, 2L, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("El orden de la parada de origen debe ser menor"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when trip does not exist")
    void shouldThrowExceptionWhenTripNotFound() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        ParcelCreateRequest request = createParcelRequest(1L, 2L, 999L);

        Parcel parcelEntity = createParcel(null, null, ParcelStatus.CREATED, fromStop, toStop, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelMapper.toEntity(request)).thenReturn(parcelEntity);
        when(parcelRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("No existe el trip con ID"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when trip belongs to different route than stops")
    void shouldThrowExceptionWhenTripBelongsToDifferentRoute() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 2L, TripStatus.SCHEDULED); // Different route
        ParcelCreateRequest request = createParcelRequest(1L, 2L, 1L);

        Parcel parcelEntity = createParcel(null, null, ParcelStatus.CREATED, fromStop, toStop, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelMapper.toEntity(request)).thenReturn(parcelEntity);
        when(parcelRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("El trip debe pertenecer a la misma ruta"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trip status is ARRIVED")
    void shouldThrowExceptionWhenTripStatusIsArrived() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.ARRIVED);
        ParcelCreateRequest request = createParcelRequest(1L, 2L, 1L);

        Parcel parcelEntity = createParcel(null, null, ParcelStatus.CREATED, fromStop, toStop, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelMapper.toEntity(request)).thenReturn(parcelEntity);
        when(parcelRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("No se puede asignar un parcel a un trip con estado"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trip status is CANCELLED")
    void shouldThrowExceptionWhenTripStatusIsCancelled() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.CANCELLED);
        ParcelCreateRequest request = createParcelRequest(1L, 2L, 1L);

        Parcel parcelEntity = createParcel(null, null, ParcelStatus.CREATED, fromStop, toStop, null);

        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelMapper.toEntity(request)).thenReturn(parcelEntity);
        when(parcelRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.createParcel(request));
        assertTrue(exception.getMessage().contains("No se puede asignar un parcel a un trip con estado"));
        verify(parcelRepository, never()).save(any());
    }

    // ========== Test cases for updateParcel ==========

    @Test
    @DisplayName("Should update parcel successfully when status is CREATED")
    void shouldUpdateParcelSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);
        ParcelUpdateRequest request = createUpdateRequest("Pedro García", "Ana Martínez");

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(existingParcel);

        // When
        parcelService.updateParcel(1L, request);

        // Then
        verify(parcelRepository).findById(1L);
        verify(parcelMapper).updateEntity(request, existingParcel);
        verify(parcelRepository).save(existingParcel);
    }

    @Test
    @DisplayName("Should throw NotFoundException when parcel does not exist for update")
    void shouldThrowExceptionWhenParcelNotFoundForUpdate() {
        // Given
        ParcelUpdateRequest request = createUpdateRequest("Pedro García", "Ana Martínez");
        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.updateParcel(999L, request));
        assertTrue(exception.getMessage().contains("Parcel no encontrado con ID"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to update parcel with status IN_TRANSIT")
    void shouldThrowExceptionWhenUpdatingParcelInTransit() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        ParcelUpdateRequest request = createUpdateRequest("Pedro García", "Ana Martínez");

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.updateParcel(1L, request));
        assertTrue(exception.getMessage().contains("Solo se pueden actualizar parcels con estado CREATED"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update parcel stops successfully when both stops are provided")
    void shouldUpdateParcelStopsSuccessfully() {
        // Given
        Stop oldFromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop oldToStop = createStop(2L, "Parada Destino", 3, 1L);
        Stop newFromStop = createStop(3L, "Nueva Origen", 1, 1L);
        Stop newToStop = createStop(4L, "Nueva Destino", 5, 1L);

        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, oldFromStop, oldToStop, null);

        ParcelUpdateRequest request = new ParcelUpdateRequest(
                "Juan Pérez",
                "3001234567",
                "María López",
                "3109876543",
                BigDecimal.valueOf(60000),
                ParcelStatus.CREATED,
                null,
                null,
                3L, // New fromStopId
                4L, // New toStopId
                null
        );

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(stopRepository.findById(3L)).thenReturn(Optional.of(newFromStop));
        when(stopRepository.findById(4L)).thenReturn(Optional.of(newToStop));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(existingParcel);

        // When
        parcelService.updateParcel(1L, request);

        // Then
        verify(stopRepository).findById(3L);
        verify(stopRepository).findById(4L);
        verify(parcelRepository).save(existingParcel);
        assertEquals(newFromStop, existingParcel.getFromStop());
        assertEquals(newToStop, existingParcel.getToStop());
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating with non-existent fromStop")
    void shouldThrowExceptionWhenUpdatingWithInvalidFromStop() {
        // Given
        Stop oldFromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop oldToStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, oldFromStop, oldToStop, null);

        ParcelUpdateRequest request = new ParcelUpdateRequest(
                "Juan Pérez", "3001234567", "María López", "3109876543",
                BigDecimal.valueOf(60000), ParcelStatus.CREATED, null, null,
                999L, null, null
        );

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.updateParcel(1L, request));
        assertTrue(exception.getMessage().contains("Stop no encontrada"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating stops belong to different routes")
    void shouldThrowExceptionWhenUpdatingStopsDifferentRoutes() {
        // Given
        Stop oldFromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop oldToStop = createStop(2L, "Parada Destino", 3, 1L);
        Stop newFromStop = createStop(3L, "Nueva Origen", 1, 1L);
        Stop newToStop = createStop(4L, "Nueva Destino", 5, 2L); // Different route

        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, oldFromStop, oldToStop, null);

        ParcelUpdateRequest request = new ParcelUpdateRequest(
                "Juan Pérez", "3001234567", "María López", "3109876543",
                BigDecimal.valueOf(60000), ParcelStatus.CREATED, null, null,
                3L, 4L, null
        );

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(stopRepository.findById(3L)).thenReturn(Optional.of(newFromStop));
        when(stopRepository.findById(4L)).thenReturn(Optional.of(newToStop));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.updateParcel(1L, request));
        assertTrue(exception.getMessage().contains("Las paradas deben pertenecer a la misma ruta"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating stops with invalid order")
    void shouldThrowExceptionWhenUpdatingStopsInvalidOrder() {
        // Given
        Stop oldFromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop oldToStop = createStop(2L, "Parada Destino", 3, 1L);
        Stop newFromStop = createStop(3L, "Nueva Origen", 5, 1L);
        Stop newToStop = createStop(4L, "Nueva Destino", 3, 1L);

        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, oldFromStop, oldToStop, null);

        ParcelUpdateRequest request = new ParcelUpdateRequest(
                "Juan Pérez", "3001234567", "María López", "3109876543",
                BigDecimal.valueOf(60000), ParcelStatus.CREATED, null, null,
                3L, 4L, null
        );

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(stopRepository.findById(3L)).thenReturn(Optional.of(newFromStop));
        when(stopRepository.findById(4L)).thenReturn(Optional.of(newToStop));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.updateParcel(1L, request));
        assertTrue(exception.getMessage().contains("El orden de origen debe ser menor al de destino"));
        verify(parcelRepository, never()).save(any());
    }

    // ========== Test cases for assignTrip ==========

    @Test
    @DisplayName("Should assign trip to parcel successfully")
    void shouldAssignTripSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.SCHEDULED);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(existingParcel);

        // When
        parcelService.assignTrip(1L, 1L);

        // Then
        verify(parcelRepository).findById(1L);
        verify(tripRepository).findById(1L);
        verify(parcelRepository).save(existingParcel);
        assertEquals(ParcelStatus.IN_TRANSIT, existingParcel.getStatus());
        assertEquals(trip, existingParcel.getTrip());
    }

    @Test
    @DisplayName("Should throw NotFoundException when parcel does not exist for trip assignment")
    void shouldThrowExceptionWhenParcelNotFoundForAssignTrip() {
        // Given
        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.assignTrip(999L, 1L));
        assertTrue(exception.getMessage().contains("Parcel no encontrado con ID"));
        verify(tripRepository, never()).findById(anyLong());
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to assign trip to parcel not in CREATED status")
    void shouldThrowExceptionWhenAssigningTripToNonCreatedParcel() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip existingTrip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, existingTrip);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.assignTrip(1L, 2L));
        assertTrue(exception.getMessage().contains("Solo se pueden asignar trips a parcels con estado CREATED"));
        verify(tripRepository, never()).findById(anyLong());
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when trip does not exist for assignment")
    void shouldThrowExceptionWhenTripNotFoundForAssignment() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.assignTrip(1L, 999L));
        assertTrue(exception.getMessage().contains("Trip no encontrado con ID"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when trip route does not match parcel route")
    void shouldThrowExceptionWhenTripRouteMismatch() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 2L, TripStatus.SCHEDULED); // Different route
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.assignTrip(1L, 1L));
        assertTrue(exception.getMessage().contains("El trip debe pertenecer a la misma ruta del parcel"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to assign parcel to ARRIVED trip")
    void shouldThrowExceptionWhenAssigningToArrivedTrip() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.ARRIVED);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.assignTrip(1L, 1L));
        assertTrue(exception.getMessage().contains("No se puede asignar un parcel a un trip con estado"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to assign parcel to CANCELLED trip")
    void shouldThrowExceptionWhenAssigningToCancelledTrip() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.CANCELLED);
        Parcel existingParcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(existingParcel));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.assignTrip(1L, 1L));
        assertTrue(exception.getMessage().contains("No se puede asignar un parcel a un trip con estado"));
        verify(parcelRepository, never()).save(any());
    }

    // ========== Test cases for getParcelByCode ==========

    @Test
    @DisplayName("Should get parcel by code successfully")
    void shouldGetParcelByCodeSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findByCode("PAQ-20250119-0001")).thenReturn(Optional.of(parcel));
        when(parcelMapper.toResponse(parcel)).thenReturn(createParcelResponse(parcel));

        // When
        ParcelResponse response = parcelService.getParcelByCode("PAQ-20250119-0001");

        // Then
        assertNotNull(response);
        assertEquals("PAQ-20250119-0001", response.code());
        verify(parcelRepository).findByCode("PAQ-20250119-0001");
    }

    @Test
    @DisplayName("Should throw NotFoundException when parcel code does not exist")
    void shouldThrowExceptionWhenParcelCodeNotFound() {
        // Given
        when(parcelRepository.findByCode("INVALID-CODE")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.getParcelByCode("INVALID-CODE"));
        assertTrue(exception.getMessage().contains("No existe ningún parcel con código"));
        verify(parcelMapper, never()).toResponse(any());
    }

    // ========== Test cases for getParcelsByTrip ==========

    @Test
    @DisplayName("Should get parcels by trip successfully")
    void shouldGetParcelsByTripSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel1 = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        Parcel parcel2 = createParcel(2L, "PAQ-20250119-0002", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);

        when(tripRepository.existsById(1L)).thenReturn(true);
        when(parcelRepository.findByTripId(1L)).thenReturn(Arrays.asList(parcel1, parcel2));
        when(parcelMapper.toResponse(any(Parcel.class))).thenReturn(createParcelResponse(parcel1));

        // When
        List<ParcelResponse> responses = parcelService.getParcelsByTrip(1L);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(tripRepository).existsById(1L);
        verify(parcelRepository).findByTripId(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when trip does not exist for getting parcels")
    void shouldThrowExceptionWhenTripNotFoundForGettingParcels() {
        // Given
        when(tripRepository.existsById(999L)).thenReturn(false);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.getParcelsByTrip(999L));
        assertTrue(exception.getMessage().contains("Trip no encontrado con ID"));
        verify(parcelRepository, never()).findByTripId(anyLong());
    }

    @Test
    @DisplayName("Should return empty list when trip has no parcels")
    void shouldReturnEmptyListWhenTripHasNoParcels() {
        // Given
        when(tripRepository.existsById(1L)).thenReturn(true);
        when(parcelRepository.findByTripId(1L)).thenReturn(List.of());

        // When
        List<ParcelResponse> responses = parcelService.getParcelsByTrip(1L);

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(parcelRepository).findByTripId(1L);
    }

    // ========== Test cases for getParcelsBySender ==========

    @Test
    @DisplayName("Should get parcels by sender phone successfully")
    void shouldGetParcelsBySenderSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel parcel1 = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);
        Parcel parcel2 = createParcel(2L, "PAQ-20250119-0002", ParcelStatus.IN_TRANSIT, fromStop, toStop, null);

        when(parcelRepository.findBySenderPhone("3001234567")).thenReturn(Arrays.asList(parcel1, parcel2));
        when(parcelMapper.toResponse(any(Parcel.class))).thenReturn(createParcelResponse(parcel1));

        // When
        List<ParcelResponse> responses = parcelService.getParcelsBySender("3001234567");

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(parcelRepository).findBySenderPhone("3001234567");
    }

    @Test
    @DisplayName("Should return empty list when sender has no parcels")
    void shouldReturnEmptyListWhenSenderHasNoParcels() {
        // Given
        when(parcelRepository.findBySenderPhone("9999999999")).thenReturn(List.of());

        // When
        List<ParcelResponse> responses = parcelService.getParcelsBySender("9999999999");

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(parcelRepository).findBySenderPhone("9999999999");
    }

    // ========== Test cases for getParcelsByReceiver ==========

    @Test
    @DisplayName("Should get parcels by receiver phone successfully")
    void shouldGetParcelsByReceiverSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel parcel1 = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findByReceiverPhone("3109876543")).thenReturn(List.of(parcel1));
        when(parcelMapper.toResponse(parcel1)).thenReturn(createParcelResponse(parcel1));

        // When
        List<ParcelResponse> responses = parcelService.getParcelsByReceiver("3109876543");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(parcelRepository).findByReceiverPhone("3109876543");
    }

    @Test
    @DisplayName("Should return empty list when receiver has no parcels")
    void shouldReturnEmptyListWhenReceiverHasNoParcels() {
        // Given
        when(parcelRepository.findByReceiverPhone("9999999999")).thenReturn(List.of());

        // When
        List<ParcelResponse> responses = parcelService.getParcelsByReceiver("9999999999");

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(parcelRepository).findByReceiverPhone("9999999999");
    }

    // ========== Test cases for confirmDelivery ==========

    @Test
    @DisplayName("Should confirm delivery successfully with correct OTP and photo")
    void shouldConfirmDeliverySuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        parcel.setDeliveryOTP("123456");

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);

        // When
        parcelService.confirmDelivery(1L, "123456", "https://example.com/proof.jpg");

        // Then
        verify(parcelRepository).findById(1L);
        verify(parcelRepository).save(parcel);
        assertEquals(ParcelStatus.DELIVERED, parcel.getStatus());
        assertEquals("https://example.com/proof.jpg", parcel.getProofPhotoUrl());
    }

    @Test
    @DisplayName("Should throw NotFoundException when parcel does not exist for delivery confirmation")
    void shouldThrowExceptionWhenParcelNotFoundForDelivery() {
        // Given
        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.confirmDelivery(999L, "123456", "https://example.com/proof.jpg"));
        assertTrue(exception.getMessage().contains("Parcel no encontrado con ID"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when parcel is not IN_TRANSIT for delivery")
    void shouldThrowExceptionWhenParcelNotInTransitForDelivery() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.confirmDelivery(1L, "123456", "https://example.com/proof.jpg"));
        assertTrue(exception.getMessage().contains("Solo se pueden entregar parcels en estado IN_TRANSIT"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mark delivery as failed when OTP is incorrect")
    void shouldMarkDeliveryFailedWhenOTPIncorrect() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        parcel.setDeliveryOTP("123456");

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);

        // When
        parcelService.confirmDelivery(1L, "WRONG-OTP", "https://example.com/proof.jpg");

        // Then
        when(parcelRepository.findById(2L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);

        // When
        parcelService.confirmDelivery(2L, "WRONG-OTP", "https://example.com/proof.jpg");

        // Then
        verify(parcelRepository, times(2)).findById(2L);
        verify(parcelRepository).save(parcel);
        assertEquals(ParcelStatus.FAILED, parcel.getStatus());
        verify(incidentService).createIncident(any(IncidentCreateRequest.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when proof photo URL is null")
    void shouldThrowExceptionWhenProofPhotoIsNull() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        parcel.setDeliveryOTP("123456");

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.confirmDelivery(1L, "123456", null));
        assertTrue(exception.getMessage().contains("Se requiere una foto de prueba"));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when proof photo URL is blank")
    void shouldThrowExceptionWhenProofPhotoIsBlank() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);
        parcel.setDeliveryOTP("123456");

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parcelService.confirmDelivery(1L, "123456", "   "));
        assertTrue(exception.getMessage().contains("Se requiere una foto de prueba"));
        verify(parcelRepository, never()).save(any());
    }

    // ========== Test cases for markDeliveryFailed ==========

    @Test
    @DisplayName("Should mark delivery as failed successfully and create incident")
    void shouldMarkDeliveryFailedSuccessfully() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);

        // When
        parcelService.markDeliveryFailed(1L, "Destinatario no encontrado");

        // Then
        verify(parcelRepository).findById(1L);
        verify(parcelRepository).save(parcel);
        assertEquals(ParcelStatus.FAILED, parcel.getStatus());
        verify(incidentService).createIncident(any(IncidentCreateRequest.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when parcel does not exist for marking failed")
    void shouldThrowExceptionWhenParcelNotFoundForMarkingFailed() {
        // Given
        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> parcelService.markDeliveryFailed(999L, "Some reason"));
        assertTrue(exception.getMessage().contains("Parcel no encontrado con ID"));
        verify(parcelRepository, never()).save(any());
        verify(incidentService, never()).createIncident(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when marking failed for non IN_TRANSIT parcel")
    void shouldThrowExceptionWhenMarkingFailedForNonInTransitParcel() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.CREATED, fromStop, toStop, null);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> parcelService.markDeliveryFailed(1L, "Some reason"));
        assertTrue(exception.getMessage().contains("Solo se pueden marcar fallidos parcels en estado IN_TRANSIT"));
        verify(parcelRepository, never()).save(any());
        verify(incidentService, never()).createIncident(any());
    }

    @Test
    @DisplayName("Should mark delivery as failed even when incident creation fails")
    void shouldMarkDeliveryFailedEvenWhenIncidentCreationFails() {
        // Given
        Stop fromStop = createStop(1L, "Parada Origen", 1, 1L);
        Stop toStop = createStop(2L, "Parada Destino", 3, 1L);
        Trip trip = createTrip(1L, 1L, TripStatus.DEPARTED);
        Parcel parcel = createParcel(1L, "PAQ-20250119-0001", ParcelStatus.IN_TRANSIT, fromStop, toStop, trip);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);
        doThrow(new RuntimeException("Incident service error")).when(incidentService).createIncident(any());

        // When
        parcelService.markDeliveryFailed(1L, "Destinatario no encontrado");

        // Then
        verify(parcelRepository).save(parcel);
        assertEquals(ParcelStatus.FAILED, parcel.getStatus());
        verify(incidentService).createIncident(any(IncidentCreateRequest.class));
    }
}