package co.unimagdalena.services;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.IncidentServiceImpl;
import co.unimagdalena.services.mapper.IncidentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private IncidentMapper incidentMapper;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    // ==================== Helper Methods ====================

    private Trip createTrip() {
        return Trip.builder()
                .id(1L)
                .build();
    }

    private Parcel createParcel() {
        return Parcel.builder()
                .id(2L)
                .build();
    }

    private Ticket createTicket() {
        return Ticket.builder()
                .id(3L)
                .build();
    }

    private Incident createIncident(Long id, EntityType entityType, Long entityId, IncidentType incidentType, String note) {
        return Incident.builder()
                .id(id)
                .entityType(entityType)
                .entityId(entityId)
                .incidentType(incidentType)
                .note(note)
                .createdAt(LocalTime.now())
                .build();
    }

    private IncidentCreateRequest createIncidentCreateRequest(EntityType entityType, Long entityId, IncidentType incidentType, String note) {
        return new IncidentCreateRequest(entityType, entityId, incidentType, note);
    }

    private IncidentUpdateRequest createIncidentUpdateRequest(EntityType entityType, Long entityId, IncidentType incidentType, String note) {
        return new IncidentUpdateRequest(entityType, entityId, incidentType, note);
    }

    private IncidentResponse createIncidentResponse(Long id, EntityType entityType, Long entityId, IncidentType incidentType, String note) {
        return new IncidentResponse(id, entityType, entityId, incidentType, note, LocalTime.now());
    }

    // ==================== Tests for createIncident ====================

    @Test
    @DisplayName("Debe crear un incidente para un TRIP exitosamente")
    void shouldCreateIncidentForTripSuccessfully() {
        // Given
        IncidentCreateRequest request = createIncidentCreateRequest(
                EntityType.TRIP, 1L, IncidentType.SECURITY, "Security issue"
        );
        Trip trip = createTrip();
        Incident incident = createIncident(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Security issue");
        IncidentResponse response = createIncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Security issue");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(incidentMapper.toEntity(request)).thenReturn(incident);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);
        when(incidentMapper.toResponse(incident)).thenReturn(response);

        // When
        IncidentResponse result = incidentService.createIncident(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.entityType()).isEqualTo(EntityType.TRIP);
        assertThat(result.entityId()).isEqualTo(1L);
        assertThat(result.incidentType()).isEqualTo(IncidentType.SECURITY);
        assertThat(result.note()).isEqualTo("Security issue");

        verify(tripRepository).findById(1L);
        verify(incidentRepository).save(any(Incident.class));
        verify(incidentMapper).toEntity(request);
        verify(incidentMapper).toResponse(incident);

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe crear un incidente para un PARCEL exitosamente")
    void shouldCreateIncidentForParcelSuccessfully() {
        // Given
        IncidentCreateRequest request = createIncidentCreateRequest(
                EntityType.PARCEL, 2L, IncidentType.DELIVERY_FAIL, "Delivery failed"
        );
        Parcel parcel = createParcel();
        Incident incident = createIncident(2L, EntityType.PARCEL, 2L, IncidentType.DELIVERY_FAIL, "Delivery failed");
        IncidentResponse response = createIncidentResponse(2L, EntityType.PARCEL, 2L, IncidentType.DELIVERY_FAIL, "Delivery failed");

        when(parcelRepository.findById(2L)).thenReturn(Optional.of(parcel));
        when(incidentMapper.toEntity(request)).thenReturn(incident);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);
        when(incidentMapper.toResponse(incident)).thenReturn(response);

        // When
        IncidentResponse result = incidentService.createIncident(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.entityType()).isEqualTo(EntityType.PARCEL);
        assertThat(result.entityId()).isEqualTo(2L);
        assertThat(result.incidentType()).isEqualTo(IncidentType.DELIVERY_FAIL);

        verify(parcelRepository).findById(2L);
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    @DisplayName("Debe crear un incidente para un TICKET exitosamente")
    void shouldCreateIncidentForTicketSuccessfully() {
        // Given
        IncidentCreateRequest request = createIncidentCreateRequest(
                EntityType.TICKET, 3L, IncidentType.OVERBOOK, "Overbooking issue"
        );
        Ticket ticket = createTicket();
        Incident incident = createIncident(3L, EntityType.TICKET, 3L, IncidentType.OVERBOOK, "Overbooking issue");
        IncidentResponse response = createIncidentResponse(3L, EntityType.TICKET, 3L, IncidentType.OVERBOOK, "Overbooking issue");

        when(ticketRepository.findById(3L)).thenReturn(Optional.of(ticket));
        when(incidentMapper.toEntity(request)).thenReturn(incident);
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);
        when(incidentMapper.toResponse(incident)).thenReturn(response);

        // When
        IncidentResponse result = incidentService.createIncident(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.entityType()).isEqualTo(EntityType.TICKET);
        assertThat(result.entityId()).isEqualTo(3L);
        assertThat(result.incidentType()).isEqualTo(IncidentType.OVERBOOK);

        verify(ticketRepository).findById(3L);
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el TRIP no existe")
    void shouldThrowExceptionWhenTripNotFound() {
        // Given
        IncidentCreateRequest request = createIncidentCreateRequest(
                EntityType.TRIP, 999L, IncidentType.VEHICLE, "Vehicle issue"
        );

        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> incidentService.createIncident(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip con ID 999 no existe");

        verify(tripRepository).findById(999L);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el PARCEL no existe")
    void shouldThrowExceptionWhenParcelNotFound() {
        // Given
        IncidentCreateRequest request = createIncidentCreateRequest(
                EntityType.PARCEL, 999L, IncidentType.DELIVERY_FAIL, "Failed delivery"
        );

        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> incidentService.createIncident(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel con ID 999 no existe");

        verify(parcelRepository).findById(999L);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el TICKET no existe")
    void shouldThrowExceptionWhenTicketNotFound() {
        // Given
        IncidentCreateRequest request = createIncidentCreateRequest(
                EntityType.TICKET, 999L, IncidentType.SECURITY, "Security breach"
        );

        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> incidentService.createIncident(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket con ID 999 no existe");

        verify(ticketRepository).findById(999L);
        verify(incidentRepository, never()).save(any());
    }

    // ==================== Tests for updateIncident ====================

    @Test
    @DisplayName("Debe actualizar un incidente exitosamente")
    void shouldUpdateIncidentSuccessfully() {
        // Given
        Long incidentId = 1L;
        IncidentUpdateRequest request = createIncidentUpdateRequest(
                EntityType.TRIP, 1L, IncidentType.VEHICLE, "Updated note"
        );
        Incident incident = createIncident(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Old note");

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(incident)).thenReturn(incident);

        // When
        incidentService.updateIncident(incidentId, request);

        // Then
        verify(incidentRepository).findById(incidentId);
        verify(incidentMapper).updateEntity(request, incident);
        verify(incidentRepository).save(incident);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el incidente a actualizar no existe")
    void shouldThrowExceptionWhenUpdatingNonExistentIncident() {
        // Given
        Long incidentId = 999L;
        IncidentUpdateRequest request = createIncidentUpdateRequest(
                EntityType.TRIP, 1L, IncidentType.SECURITY, "Note"
        );

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> incidentService.updateIncident(incidentId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incidente con ID 999 no existe");

        verify(incidentRepository).findById(incidentId);
        verify(incidentMapper, never()).updateEntity(any(), any());
        verify(incidentRepository, never()).save(any());
    }

    // ==================== Tests for deleteIncident ====================

    @Test
    @DisplayName("Debe eliminar un incidente exitosamente")
    void shouldDeleteIncidentSuccessfully() {
        // Given
        Long incidentId = 1L;
        Incident incident = createIncident(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Note");

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));

        // When
        incidentService.deleteIncident(incidentId);

        // Then
        verify(incidentRepository).findById(incidentId);
        verify(incidentRepository).delete(incident);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el incidente a eliminar no existe")
    void shouldThrowExceptionWhenDeletingNonExistentIncident() {
        // Given
        Long incidentId = 999L;

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> incidentService.deleteIncident(incidentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incidente con ID 999 no existe");

        verify(incidentRepository).findById(incidentId);
        verify(incidentRepository, never()).delete(any());
    }

    // ==================== Tests for resolveIncident ====================

    @Test
    @DisplayName("Debe resolver un incidente exitosamente (eliminar)")
    void shouldResolveIncidentSuccessfully() {
        // Given
        Long incidentId = 1L;
        Incident incident = createIncident(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Note");

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));

        // When
        incidentService.resolveIncident(incidentId);

        // Then
        verify(incidentRepository).findById(incidentId);
        verify(incidentRepository).delete(incident);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el incidente a resolver no existe")
    void shouldThrowExceptionWhenResolvingNonExistentIncident() {
        // Given
        Long incidentId = 999L;

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> incidentService.resolveIncident(incidentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Incidente con ID 999 no existe");

        verify(incidentRepository).findById(incidentId);
        verify(incidentRepository, never()).delete(any());
    }

    // ==================== Tests for getTotalDelayMinutes ====================

    @Test
    @DisplayName("Debe retornar 1 cuando existe un incidente de tipo DELIVERY_FAIL para el trip")
    void shouldReturnOneWhenDeliveryFailIncidentExists() {
        // Given
        Long tripId = 1L;
        Incident incident = createIncident(1L, EntityType.TRIP, tripId, IncidentType.DELIVERY_FAIL, "Delay");

        when(incidentRepository.findIncidentByEntityId(tripId)).thenReturn(Optional.of(incident));

        // When
        long result = incidentService.getTotalDelayMinutes(tripId);

        // Then
        assertThat(result).isEqualTo(1L);
        verify(incidentRepository).findIncidentByEntityId(tripId);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no existe un incidente de tipo DELIVERY_FAIL para el trip")
    void shouldReturnZeroWhenNoDeliveryFailIncidentExists() {
        // Given
        Long tripId = 1L;
        Incident incident = createIncident(1L, EntityType.TRIP, tripId, IncidentType.SECURITY, "Not a delay");

        when(incidentRepository.findIncidentByEntityId(tripId)).thenReturn(Optional.of(incident));

        // When
        long result = incidentService.getTotalDelayMinutes(tripId);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(incidentRepository).findIncidentByEntityId(tripId);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando no existe ningún incidente para el trip")
    void shouldReturnZeroWhenNoIncidentExistsForTrip() {
        // Given
        Long tripId = 1L;

        when(incidentRepository.findIncidentByEntityId(tripId)).thenReturn(Optional.empty());

        // When
        long result = incidentService.getTotalDelayMinutes(tripId);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(incidentRepository).findIncidentByEntityId(tripId);
    }

    // ==================== Tests for getActiveIncidentsByTrip ====================

    @Test
    @DisplayName("Debe obtener incidentes activos por trip exitosamente")
    void shouldGetActiveIncidentsByTripSuccessfully() {
        // Given
        Long tripId = 1L;
        Incident incident1 = createIncident(1L, EntityType.TRIP, tripId, IncidentType.SECURITY, "Issue 1");
        Incident incident2 = createIncident(2L, EntityType.TRIP, tripId, IncidentType.VEHICLE, "Issue 2");
        Incident incident3 = createIncident(3L, EntityType.PARCEL, 2L, IncidentType.DELIVERY_FAIL, "Not a trip");

        IncidentResponse response1 = createIncidentResponse(1L, EntityType.TRIP, tripId, IncidentType.SECURITY, "Issue 1");
        IncidentResponse response2 = createIncidentResponse(2L, EntityType.TRIP, tripId, IncidentType.VEHICLE, "Issue 2");

        when(incidentRepository.findIncidentByEntityId(tripId)).thenReturn(Optional.of(incident1));
        when(incidentMapper.toResponse(incident1)).thenReturn(response1);

        // When
        List<IncidentResponse> result = incidentService.getActiveIncidentsByTrip(tripId);

        // Then
        assertThat(result).isNotEmpty();
        verify(incidentRepository).findIncidentByEntityId(tripId);
    }

    @Test
    @DisplayName("Debe filtrar solo incidentes de tipo TRIP")
    void shouldFilterOnlyTripTypeIncidents() {
        // Given
        Long tripId = 1L;
        Incident incident = createIncident(1L, EntityType.PARCEL, tripId, IncidentType.DELIVERY_FAIL, "Not a trip incident");

        when(incidentRepository.findIncidentByEntityId(tripId)).thenReturn(Optional.of(incident));

        // When
        List<IncidentResponse> result = incidentService.getActiveIncidentsByTrip(tripId);

        // Then
        assertThat(result).isEmpty();
        verify(incidentRepository).findIncidentByEntityId(tripId);
        verify(incidentMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay incidentes para el trip")
    void shouldReturnEmptyListWhenNoIncidentsForTrip() {
        // Given
        Long tripId = 1L;

        when(incidentRepository.findIncidentByEntityId(tripId)).thenReturn(Optional.empty());

        // When
        List<IncidentResponse> result = incidentService.getActiveIncidentsByTrip(tripId);

        // Then
        assertThat(result).isEmpty();
        verify(incidentRepository).findIncidentByEntityId(tripId);
    }

    // ==================== Tests for getOpenIncidentsByRoute ====================

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException para getOpenIncidentsByRoute")
    void shouldThrowUnsupportedOperationExceptionForGetOpenIncidentsByRoute() {
        // Given
        Long routeId = 1L;

        // When & Then
        assertThatThrownBy(() -> incidentService.getOpenIncidentsByRoute(routeId))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("EntityType ROUTE no existe en el modelo actual");
    }

    // ==================== Tests for findByIncidentByEntityType ====================

    @Test
    @DisplayName("Debe encontrar incidentes por EntityType exitosamente")
    void shouldFindIncidentsByEntityTypeSuccessfully() {
        // Given
        EntityType type = EntityType.TRIP;
        Incident incident1 = createIncident(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Issue 1");
        Incident incident2 = createIncident(2L, EntityType.TRIP, 2L, IncidentType.VEHICLE, "Issue 2");

        IncidentResponse response1 = createIncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Issue 1");
        IncidentResponse response2 = createIncidentResponse(2L, EntityType.TRIP, 2L, IncidentType.VEHICLE, "Issue 2");

        when(incidentRepository.findByEntityType(type)).thenReturn(List.of(incident1, incident2));
        when(incidentMapper.toResponse(incident1)).thenReturn(response1);
        when(incidentMapper.toResponse(incident2)).thenReturn(response2);

        // When
        List<IncidentResponse> result = incidentService.findByIncidentByEntityType(type);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).entityType()).isEqualTo(EntityType.TRIP);
        assertThat(result.get(1).entityType()).isEqualTo(EntityType.TRIP);

        verify(incidentRepository).findByEntityType(type);
        verify(incidentMapper, times(2)).toResponse(any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay incidentes del EntityType especificado")
    void shouldReturnEmptyListWhenNoIncidentsOfEntityType() {
        // Given
        EntityType type = EntityType.PARCEL;

        when(incidentRepository.findByEntityType(type)).thenReturn(List.of());

        // When
        List<IncidentResponse> result = incidentService.findByIncidentByEntityType(type);

        // Then
        assertThat(result).isEmpty();
        verify(incidentRepository).findByEntityType(type);
        verify(incidentMapper, never()).toResponse(any());
    }

    // ==================== Tests for findIncidentsRecentByType ====================

    @Test
    @DisplayName("Debe encontrar incidentes recientes por IncidentType exitosamente")
    void shouldFindRecentIncidentsByTypeSuccessfully() {
        // Given
        IncidentType type = IncidentType.SECURITY;
        Incident incident1 = createIncident(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Recent 1");
        Incident incident2 = createIncident(2L, EntityType.TICKET, 2L, IncidentType.SECURITY, "Recent 2");

        IncidentResponse response1 = createIncidentResponse(1L, EntityType.TRIP, 1L, IncidentType.SECURITY, "Recent 1");
        IncidentResponse response2 = createIncidentResponse(2L, EntityType.TICKET, 2L, IncidentType.SECURITY, "Recent 2");

        when(incidentRepository.findRecentByType(type)).thenReturn(List.of(incident1, incident2));
        when(incidentMapper.toResponse(incident1)).thenReturn(response1);
        when(incidentMapper.toResponse(incident2)).thenReturn(response2);

        // When
        List<IncidentResponse> result = incidentService.findIncidentsRecentByType(type);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).incidentType()).isEqualTo(IncidentType.SECURITY);
        assertThat(result.get(1).incidentType()).isEqualTo(IncidentType.SECURITY);

        verify(incidentRepository).findRecentByType(type);
        verify(incidentMapper, times(2)).toResponse(any());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay incidentes recientes del tipo especificado")
    void shouldReturnEmptyListWhenNoRecentIncidentsOfType() {
        // Given
        IncidentType type = IncidentType.OVERBOOK;

        when(incidentRepository.findRecentByType(type)).thenReturn(List.of());

        // When
        List<IncidentResponse> result = incidentService.findIncidentsRecentByType(type);

        // Then
        assertThat(result).isEmpty();
        verify(incidentRepository).findRecentByType(type);
        verify(incidentMapper, never()).toResponse(any());
    }
}
