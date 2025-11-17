package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.StopDto;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.services.mapper.StopMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class StopMapperTest {
    private final StopMapper mapper = Mappers.getMapper(StopMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        StopDto.StopCreateRequest request = new StopDto.StopCreateRequest(
                "Central Station",
                1,
                4.6097,
                -74.0817,
                1L
        );

        Stop stop = mapper.toEntity(request);

        assertThat(stop.getName()).isEqualTo("Central Station");
        assertThat(stop.getOrder()).isEqualTo(1);
        assertThat(stop.getLatitude()).isEqualTo(4.6097);
        assertThat(stop.getLongitude()).isEqualTo(-74.0817);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Stop stop = Stop.builder()
                .id(1L)
                .name("Central Station")
                .order(1)
                .latitude(4.6097)
                .longitude(-74.0817)
                .build();

        StopDto.StopResponse dto = mapper.toResponse(stop);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Central Station");
        assertThat(dto.stopOrder()).isEqualTo(1);
        assertThat(dto.latitude()).isEqualTo(4.6097);
        assertThat(dto.longitude()).isEqualTo(-74.0817);
    }

    @Test
    void toSummary_shouldMapEntity() {
        Stop stop = Stop.builder()
                .id(1L)
                .name("Central Station")
                .order(1)
                .build();

        StopDto.StopSummary dto = mapper.toSummary(stop);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Central Station");
        assertThat(dto.stopOrder()).isEqualTo(1);
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Stop stop = Stop.builder()
                .id(1L)
                .name("Central Station")
                .order(1)
                .latitude(4.6097)
                .build();

        StopDto.StopUpdateRequest update = new StopDto.StopUpdateRequest(
                "Main Terminal",
                2,
                4.7110,
                null,
                null
        );

        mapper.updateEntity(update, stop);

        assertThat(stop.getName()).isEqualTo("Main Terminal");
        assertThat(stop.getOrder()).isEqualTo(2);
        assertThat(stop.getLatitude()).isEqualTo(4.7110);
    }
}
