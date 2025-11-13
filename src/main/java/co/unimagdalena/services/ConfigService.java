package co.unimagdalena.services;

import co.unimagdalena.api.dto.ConfigDto.*;

import java.math.BigDecimal;
import java.util.List;

public interface ConfigService {

    ConfigResponse createConfig(ConfigCreateRequest request);
    void updateConfig(Long id, ConfigUpdateRequest request);
    void deleteConfig(Long id);

    ConfigResponse getConfigByKey(String key);
    List<ConfigResponse> getAllConfigs();

    String getValueAsString(String key);
    BigDecimal getValueAsBigDecimal(String key);
    Integer getValueAsInt(String key);
}