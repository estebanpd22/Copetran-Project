package co.unimagdalena.services;

import co.unimagdalena.api.dto.ConfigDto.*;
import co.unimagdalena.domine.entities.Config;
import co.unimagdalena.domine.repositories.ConfigRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.impl.ConfigServiceImpl;
import co.unimagdalena.services.mapper.ConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private ConfigRepository repository;

    @Mock
    private ConfigMapper mapper;

    @InjectMocks
    private ConfigServiceImpl service;

    private Config config;
    private ConfigCreateRequest createRequest;
    private ConfigUpdateRequest updateRequest;
    private ConfigResponse response;

    @BeforeEach
    void setUp() {
        config = createConfig();
        createRequest = createConfigCreateRequest();
        updateRequest = createConfigUpdateRequest();
        response = createConfigResponse();
    }

    // ==================== CREATE CONFIG TESTS ====================

    @Test
    @DisplayName("Should create config successfully")
    void shouldCreateConfigSuccessfully() {
        // Given
        when(repository.existsByKeyIgnoreCase(createRequest.key())).thenReturn(false);
        when(mapper.toEntity(createRequest)).thenReturn(config);
        when(repository.save(config)).thenReturn(config);
        when(mapper.toResponse(config)).thenReturn(response);

        // When
        ConfigResponse result = service.createConfig(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(response.key(), result.key());
        assertEquals(response.value(), result.value());
        verify(repository).existsByKeyIgnoreCase(createRequest.key());
        verify(mapper).toEntity(createRequest);
        verify(repository).save(config);
        verify(mapper).toResponse(config);
    }

    @Test
    @DisplayName("Should throw exception when creating config with duplicate key")
    void shouldThrowExceptionWhenCreatingConfigWithDuplicateKey() {
        // Given
        when(repository.existsByKeyIgnoreCase(createRequest.key())).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.createConfig(createRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository).existsByKeyIgnoreCase(createRequest.key());
        verify(mapper, never()).toEntity(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should create config ignoring case sensitivity for duplicate check")
    void shouldCreateConfigIgnoringCaseSensitivityForDuplicateCheck() {
        // Given
        ConfigCreateRequest upperCaseRequest = new ConfigCreateRequest("MAX_BAGGAGE_WEIGHT", "50.0");
        when(repository.existsByKeyIgnoreCase(upperCaseRequest.key())).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.createConfig(upperCaseRequest));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository).existsByKeyIgnoreCase(upperCaseRequest.key());
    }

    // ==================== UPDATE CONFIG TESTS ====================

    @Test
    @DisplayName("Should update config successfully")
    void shouldUpdateConfigSuccessfully() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(config));
        when(repository.save(config)).thenReturn(config);
        doNothing().when(mapper).updateEntity(updateRequest, config);

        // When
        service.updateConfig(1L, updateRequest);

        // Then
        verify(repository).findById(1L);
        verify(mapper).updateEntity(updateRequest, config);
        verify(repository).save(config);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent config")
    void shouldThrowExceptionWhenUpdatingNonExistentConfig() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.updateConfig(999L, updateRequest));

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository).findById(999L);
        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should update config with null values ignored by mapper")
    void shouldUpdateConfigWithNullValuesIgnoredByMapper() {
        // Given
        ConfigUpdateRequest partialUpdate = new ConfigUpdateRequest(null, "75.5");
        when(repository.findById(1L)).thenReturn(Optional.of(config));
        when(repository.save(config)).thenReturn(config);
        doNothing().when(mapper).updateEntity(partialUpdate, config);

        // When
        service.updateConfig(1L, partialUpdate);

        // Then
        verify(repository).findById(1L);
        verify(mapper).updateEntity(partialUpdate, config);
        verify(repository).save(config);
    }

    // ==================== DELETE CONFIG TESTS ====================

    @Test
    @DisplayName("Should delete config successfully")
    void shouldDeleteConfigSuccessfully() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        // When
        service.deleteConfig(1L);

        // Then
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent config")
    void shouldThrowExceptionWhenDeletingNonExistentConfig() {
        // Given
        when(repository.existsById(999L)).thenReturn(false);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.deleteConfig(999L));

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    // ==================== GET CONFIG BY KEY TESTS ====================

    @Test
    @DisplayName("Should get config by key successfully")
    void shouldGetConfigByKeySuccessfully() {
        // Given
        when(repository.findByKey("max_baggage_weight")).thenReturn(Optional.of(config));
        when(mapper.toResponse(config)).thenReturn(response);

        // When
        ConfigResponse result = service.getConfigByKey("max_baggage_weight");

        // Then
        assertNotNull(result);
        assertEquals(response.key(), result.key());
        assertEquals(response.value(), result.value());
        verify(repository).findByKey("max_baggage_weight");
        verify(mapper).toResponse(config);
    }

    @Test
    @DisplayName("Should throw exception when config key not found")
    void shouldThrowExceptionWhenConfigKeyNotFound() {
        // Given
        when(repository.findByKey("non_existent_key")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getConfigByKey("non_existent_key"));

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository).findByKey("non_existent_key");
        verify(mapper, never()).toResponse(any());
    }

    // ==================== GET ALL CONFIGS TESTS ====================

    @Test
    @DisplayName("Should get all configs successfully")
    void shouldGetAllConfigsSuccessfully() {
        // Given
        Config config2 = Config.builder()
                .id(2L)
                .key("min_purchase_amount")
                .value("10.0")
                .build();

        ConfigResponse response2 = new ConfigResponse(2L, "min_purchase_amount", "10.0");

        when(repository.findAll()).thenReturn(List.of(config, config2));
        when(mapper.toResponse(config)).thenReturn(response);
        when(mapper.toResponse(config2)).thenReturn(response2);

        // When
        List<ConfigResponse> results = service.getAllConfigs();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(response.key(), results.get(0).key());
        assertEquals(response2.key(), results.get(1).key());
        verify(repository).findAll();
        verify(mapper, times(2)).toResponse(any(Config.class));
    }

    @Test
    @DisplayName("Should return empty list when no configs exist")
    void shouldReturnEmptyListWhenNoConfigsExist() {
        // Given
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<ConfigResponse> results = service.getAllConfigs();

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(repository).findAll();
        verify(mapper, never()).toResponse(any());
    }

    // ==================== GET VALUE AS STRING TESTS ====================

    @Test
    @DisplayName("Should get value as string successfully")
    void shouldGetValueAsStringSuccessfully() {
        // Given
        when(repository.findByKey("max_baggage_weight")).thenReturn(Optional.of(config));

        // When
        String result = service.getValueAsString("max_baggage_weight");

        // Then
        assertNotNull(result);
        assertEquals("50.0", result);
        verify(repository).findByKey("max_baggage_weight");
    }

    @Test
    @DisplayName("Should throw exception when getting string value for non-existent key")
    void shouldThrowExceptionWhenGettingStringValueForNonExistentKey() {
        // Given
        when(repository.findByKey("non_existent_key")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getValueAsString("non_existent_key"));

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository).findByKey("non_existent_key");
    }

    // ==================== GET VALUE AS BIG DECIMAL TESTS ====================

    @Test
    @DisplayName("Should get value as BigDecimal successfully")
    void shouldGetValueAsBigDecimalSuccessfully() {
        // Given
        when(repository.findByKey("max_baggage_weight")).thenReturn(Optional.of(config));

        // When
        BigDecimal result = service.getValueAsBigDecimal("max_baggage_weight");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("50.0"), result);
        verify(repository).findByKey("max_baggage_weight");
    }

    @Test
    @DisplayName("Should throw exception when getting BigDecimal value for non-existent key")
    void shouldThrowExceptionWhenGettingBigDecimalValueForNonExistentKey() {
        // Given
        when(repository.findByKey("non_existent_key")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getValueAsBigDecimal("non_existent_key"));

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository).findByKey("non_existent_key");
    }

    @Test
    @DisplayName("Should throw exception when value is not a valid BigDecimal")
    void shouldThrowExceptionWhenValueIsNotValidBigDecimal() {
        // Given
        Config invalidConfig = Config.builder()
                .id(1L)
                .key("max_baggage_weight")
                .value("not_a_number")
                .build();

        when(repository.findByKey("max_baggage_weight")).thenReturn(Optional.of(invalidConfig));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getValueAsBigDecimal("max_baggage_weight"));

        assertTrue(exception.getMessage().contains("does not contain a valid decimal value"));
        verify(repository).findByKey("max_baggage_weight");
    }

    @Test
    @DisplayName("Should throw exception when BigDecimal value is empty string")
    void shouldThrowExceptionWhenBigDecimalValueIsEmptyString() {
        // Given
        Config emptyConfig = Config.builder()
                .id(1L)
                .key("max_baggage_weight")
                .value("")
                .build();

        when(repository.findByKey("max_baggage_weight")).thenReturn(Optional.of(emptyConfig));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getValueAsBigDecimal("max_baggage_weight"));

        assertTrue(exception.getMessage().contains("does not contain a valid decimal value"));
        verify(repository).findByKey("max_baggage_weight");
    }

    // ==================== GET VALUE AS INTEGER TESTS ====================

    @Test
    @DisplayName("Should get value as Integer successfully")
    void shouldGetValueAsIntegerSuccessfully() {
        // Given
        Config intConfig = Config.builder()
                .id(1L)
                .key("max_passengers")
                .value("50")
                .build();

        when(repository.findByKey("max_passengers")).thenReturn(Optional.of(intConfig));

        // When
        Integer result = service.getValueAsInt("max_passengers");

        // Then
        assertNotNull(result);
        assertEquals(50, result);
        verify(repository).findByKey("max_passengers");
    }

    @Test
    @DisplayName("Should throw exception when getting Integer value for non-existent key")
    void shouldThrowExceptionWhenGettingIntegerValueForNonExistentKey() {
        // Given
        when(repository.findByKey("non_existent_key")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getValueAsInt("non_existent_key"));

        assertTrue(exception.getMessage().contains("not found"));
        verify(repository).findByKey("non_existent_key");
    }

    @Test
    @DisplayName("Should throw exception when value is not a valid Integer")
    void shouldThrowExceptionWhenValueIsNotValidInteger() {
        // Given
        Config invalidConfig = Config.builder()
                .id(1L)
                .key("max_passengers")
                .value("not_a_number")
                .build();

        when(repository.findByKey("max_passengers")).thenReturn(Optional.of(invalidConfig));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getValueAsInt("max_passengers"));

        assertTrue(exception.getMessage().contains("does not contain a valid integer value"));
        verify(repository).findByKey("max_passengers");
    }

    @Test
    @DisplayName("Should throw exception when Integer value is decimal")
    void shouldThrowExceptionWhenIntegerValueIsDecimal() {
        // Given
        Config decimalConfig = Config.builder()
                .id(1L)
                .key("max_passengers")
                .value("50.5")
                .build();

        when(repository.findByKey("max_passengers")).thenReturn(Optional.of(decimalConfig));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getValueAsInt("max_passengers"));

        assertTrue(exception.getMessage().contains("does not contain a valid integer value"));
        verify(repository).findByKey("max_passengers");
    }

    @Test
    @DisplayName("Should throw exception when Integer value is empty string")
    void shouldThrowExceptionWhenIntegerValueIsEmptyString() {
        // Given
        Config emptyConfig = Config.builder()
                .id(1L)
                .key("max_passengers")
                .value("")
                .build();

        when(repository.findByKey("max_passengers")).thenReturn(Optional.of(emptyConfig));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getValueAsInt("max_passengers"));

        assertTrue(exception.getMessage().contains("does not contain a valid integer value"));
        verify(repository).findByKey("max_passengers");
    }

    // ==================== HELPER METHODS ====================

    private Config createConfig() {
        return Config.builder()
                .id(1L)
                .key("max_baggage_weight")
                .value("50.0")
                .build();
    }

    private ConfigCreateRequest createConfigCreateRequest() {
        return new ConfigCreateRequest(
                "max_baggage_weight",
                "50.0"
        );
    }

    private ConfigUpdateRequest createConfigUpdateRequest() {
        return new ConfigUpdateRequest(
                "max_baggage_weight",
                "75.0"
        );
    }

    private ConfigResponse createConfigResponse() {
        return new ConfigResponse(
                1L,
                "max_baggage_weight",
                "50.0"
        );
    }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
