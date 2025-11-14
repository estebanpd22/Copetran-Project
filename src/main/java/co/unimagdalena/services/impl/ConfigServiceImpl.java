package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.ConfigDto.*;
import co.unimagdalena.domine.entities.Config;
import co.unimagdalena.domine.repositories.ConfigRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.ConfigService;
import co.unimagdalena.services.mapper.ConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository repository;
    private final ConfigMapper mapper;

    @Override
    public ConfigResponse createConfig(ConfigCreateRequest request) {

        if (repository.existsByKeyIgnoreCase(request.key())) {
            throw new IllegalStateException(
                    "A configuration with key '" + request.key() + "' already exists."
            );
        }

        Config config = mapper.toEntity(request);
        repository.save(config);

        return mapper.toResponse(config);
    }

    @Override
    public void updateConfig(Long id, ConfigUpdateRequest request) {

        Config config = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Config with ID " + id + " not found")
                );

        mapper.updateEntity(request, config);
        repository.save(config);
    }

    @Override
    public void deleteConfig(Long id) {

        if (!repository.existsById(id)) {
            throw new NotFoundException("Config with ID " + id + " not found");
        }

        repository.deleteById(id);
    }

    @Override
    public ConfigResponse getConfigByKey(String key) {

        Config config = repository.findByKey(key)
                .orElseThrow(() ->
                        new NotFoundException("Config with key '" + key + "' not found")
                );

        return mapper.toResponse(config);
    }

    @Override
    public List<ConfigResponse> getAllConfigs() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public String getValueAsString(String key) {
        return getConfigEntity(key).getValue();
    }

    @Override
    public BigDecimal getValueAsBigDecimal(String key) {

        String value = getConfigEntity(key).getValue();

        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Config '" + key + "' does not contain a valid decimal value: " + value
            );
        }
    }

    @Override
    public Integer getValueAsInt(String key) {

        String value = getConfigEntity(key).getValue();

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Config '" + key + "' does not contain a valid integer value: " + value
            );
        }
    }

    // --- Método privado auxiliar para evitar repetir lógica ---
    private Config getConfigEntity(String key) {

        return repository.findByKey(key)
                .orElseThrow(() ->
                        new NotFoundException("Config with key '" + key + "' not found")
                );
    }
}
