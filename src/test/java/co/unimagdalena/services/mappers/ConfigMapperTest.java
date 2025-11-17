package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.ConfigDto;
import co.unimagdalena.domine.entities.Config;
import co.unimagdalena.services.mapper.ConfigMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigMapperTest {
    private final ConfigMapper mapper = Mappers.getMapper(ConfigMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        ConfigDto.ConfigCreateRequest request = new ConfigDto.ConfigCreateRequest(
                "max_seat_hold_time",
                "15"
        );

        Config config = mapper.toEntity(request);

        assertThat(config.getKey()).isEqualTo("max_seat_hold_time");
        assertThat(config.getValue()).isEqualTo("15");
    }

    @Test
    void toResponse_shouldMapEntity() {
        Config config = Config.builder()
                .id(1L)
                .key("max_seat_hold_time")
                .value("15")
                .build();

        ConfigDto.ConfigResponse dto = mapper.toResponse(config);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.key()).isEqualTo("max_seat_hold_time");
        assertThat(dto.value()).isEqualTo("15");
    }

    @Test
    void updateEntity_shouldUpdateValue() {
        Config config = Config.builder()
                .id(1L)
                .key("max_seat_hold_time")
                .value("15")
                .build();

        ConfigDto.ConfigUpdateRequest update = new ConfigDto.ConfigUpdateRequest(
                null,
                "20"
        );

        mapper.updateEntity(update, config);

        assertThat(config.getValue()).isEqualTo("20");
    }
}
