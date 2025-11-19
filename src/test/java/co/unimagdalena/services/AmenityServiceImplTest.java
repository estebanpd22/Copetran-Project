package co.unimagdalena.services;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.domine.entities.Amenity;
import co.unimagdalena.domine.repositories.AmenityRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.AmenityServiceImpl;
import co.unimagdalena.services.mapper.AmenityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmenityServiceImplTest {

    @Mock
    private AmenityRepository repository;

    @Mock
    private AmenityMapper mapper;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    private Amenity amenity;
    private AmenityCreateRequest createRequest;
    private AmenityUpdateRequest updateRequest;
    private AmenityResponse amenityResponse;

    @BeforeEach
    void setUp() {
        amenity = createAmenity();
        createRequest = createAmenityCreateRequest();
        updateRequest = createAmenityUpdateRequest();
        amenityResponse = createAmenityResponse();
    }

    // ==================== HELPER METHODS ====================
    private Amenity createAmenity() {
        return Amenity.builder()
                .id(1L)
                .name("WiFi")
                .build();
    }

    private AmenityCreateRequest createAmenityCreateRequest() {
        return new AmenityCreateRequest("WiFi");
    }

    private AmenityUpdateRequest createAmenityUpdateRequest() {
        return new AmenityUpdateRequest("USB Charging");
    }

    private AmenityResponse createAmenityResponse() {
        return new AmenityResponse(1L, "WiFi");
    }

    // ==================== CREATE AMENITY TESTS ====================
    @Test
    @DisplayName("Should create amenity successfully")
    void shouldCreateAmenity() {
        when(repository.existsByNameIgnoreCase(createRequest.name())).thenReturn(false);
        when(mapper.toEntity(createRequest)).thenReturn(amenity);
        when(mapper.toResponse(amenity)).thenReturn(amenityResponse);

        AmenityResponse result = amenityService.createAmenity(createRequest);

        assertNotNull(result);
        assertEquals("WiFi", result.name());
        verify(repository).existsByNameIgnoreCase(createRequest.name());
        verify(repository).save(amenity);
        verify(mapper).toResponse(amenity);
    }

    @Test
    @DisplayName("Should throw exception when amenity name already exists")
    void shouldThrowExceptionWhenAmenityNameAlreadyExists() {
        when(repository.existsByNameIgnoreCase(createRequest.name())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> amenityService.createAmenity(createRequest));
        verify(repository).existsByNameIgnoreCase(createRequest.name());
        verify(repository, never()).save(any());
    }

    // ==================== UPDATE AMENITY TESTS ====================
    @Test
    @DisplayName("Should update amenity successfully")
    void shouldUpdateAmenity() {
        when(repository.findById(1L)).thenReturn(Optional.of(amenity));
        when(mapper.toResponse(amenity)).thenReturn(amenityResponse);
        doNothing().when(mapper).updateEntity(updateRequest, amenity);

        AmenityResponse result = amenityService.updateAmenity(1L, updateRequest);

        assertNotNull(result);
        verify(repository).findById(1L);
        verify(mapper).updateEntity(updateRequest, amenity);
        verify(mapper).toResponse(amenity);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent amenity")
    void shouldThrowExceptionWhenUpdatingNonExistentAmenity() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> amenityService.updateAmenity(1L, updateRequest));
        verify(repository).findById(1L);
        verify(mapper, never()).updateEntity(any(), any());
    }

    // ==================== DELETE AMENITY TESTS ====================
    @Test
    @DisplayName("Should delete amenity successfully")
    void shouldDeleteAmenity() {
        when(repository.existsById(1L)).thenReturn(true);

        amenityService.deleteAmenity(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent amenity")
    void shouldThrowExceptionWhenDeletingNonExistentAmenity() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> amenityService.deleteAmenity(1L));
        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(any());
    }

    // ==================== GET BY ID TESTS ====================
    @Test
    @DisplayName("Should get amenity by id successfully")
    void shouldGetAmenityById() {
        when(repository.findById(1L)).thenReturn(Optional.of(amenity));
        when(mapper.toResponse(amenity)).thenReturn(amenityResponse);

        AmenityResponse result = amenityService.getAmenityById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("WiFi", result.name());
        verify(repository).findById(1L);
        verify(mapper).toResponse(amenity);
    }

    @Test
    @DisplayName("Should throw exception when amenity not found by id")
    void shouldThrowExceptionWhenAmenityNotFoundById() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> amenityService.getAmenityById(1L));
        verify(repository).findById(1L);
        verify(mapper, never()).toResponse(any());
    }

    // ==================== GET ALL AMENITIES TESTS ====================
    @Test
    @DisplayName("Should get all amenities successfully")
    void shouldGetAllAmenities() {
        List<Amenity> amenities = List.of(amenity);
        when(repository.findAll()).thenReturn(amenities);
        when(mapper.toResponse(amenity)).thenReturn(amenityResponse);

        List<AmenityResponse> result = amenityService.getAllAmenities();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findAll();
        verify(mapper).toResponse(amenity);
    }

    @Test
    @DisplayName("Should return empty list when no amenities exist")
    void shouldReturnEmptyListWhenNoAmenitiesExist() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<AmenityResponse> result = amenityService.getAllAmenities();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
        verify(mapper, never()).toResponse(any());
    }
}
