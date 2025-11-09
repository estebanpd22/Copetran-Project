package co.unimagdalena.repositories;

import co.unimagdalena.domine.entities.Config;
import co.unimagdalena.domine.repositories.ConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigRepositoryTest extends AbstractRepositoryTI {
    @Autowired
    private ConfigRepository configRepository;

    private Config createConfig(String key, String value) {
        return configRepository.save(Config.builder()
                .key(key)
                .value(value)
                .build());
    }

    @Test
    @DisplayName("Debe encontrar una configuración por su clave")
    void shouldFindByKey() {
        // Given
        Config config = createConfig("max_baggage_weight", "25.0");

        // When
        Optional<Config> found = configRepository.findByKey("max_baggage_weight");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getKey()).isEqualTo("max_baggage_weight");
        assertThat(found.get().getValue()).isEqualTo("25.0");
    }

    @Test
    @DisplayName("Debe retornar empty cuando no existe la configuración")
    void shouldReturnEmptyWhenConfigNotFound() {
        // When
        Optional<Config> found = configRepository.findByKey("non_existent_key");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe guardar múltiples configuraciones")
    void shouldSaveMultipleConfigs() {
        // Given
        createConfig("seat_hold_timeout", "300");
        createConfig("cancellation_deadline_hours", "24");

        // When
        Optional<Config> config1 = configRepository.findByKey("seat_hold_timeout");
        Optional<Config> config2 = configRepository.findByKey("cancellation_deadline_hours");

        // Then
        assertThat(config1).isPresent();
        assertThat(config1.get().getValue()).isEqualTo("300");
        assertThat(config2).isPresent();
        assertThat(config2.get().getValue()).isEqualTo("24");
    }
}