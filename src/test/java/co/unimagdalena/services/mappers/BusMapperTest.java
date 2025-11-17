package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.BusDto;
import co.unimagdalena.domine.entities.Bus;
import co.unimagdalena.domine.entities.BusStatus;
import co.unimagdalena.services.mapper.BusMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class BusMapperTest {
    private final BusMapper mapper = Mappers.getMapper(BusMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        BusDto.BusCreateRequest request = new BusDto.BusCreateRequest(
                "ABC-123",
                40,
                BusStatus.AVAILABLE,
                new HashSet<>()
        );

        Bus bus = mapper.toEntity(request);

        assertThat(bus.getPlate()).isEqualTo("ABC-123");
        assertThat(bus.getCapacity()).isEqualTo(40);
        assertThat(bus.getStatus()).isEqualTo(BusStatus.AVAILABLE);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Bus bus = Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .build();

        BusDto.BusResponse dto = mapper.toResponse(bus);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.plate()).isEqualTo("ABC-123");
        assertThat(dto.capacity()).isEqualTo(40);
        assertThat(dto.status()).isEqualTo(BusStatus.AVAILABLE);
    }

    @Test
    void toSummary_shouldMapEntity() {
        Bus bus = Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .build();

        BusDto.BusSummary dto = mapper.toSummary(bus);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.plate()).isEqualTo("ABC-123");
        assertThat(dto.capacity()).isEqualTo(40);
        assertThat(dto.status()).isEqualTo(BusStatus.AVAILABLE);
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Bus bus = Bus.builder()
                .id(1L)
                .plate("ABC-123")
                .capacity(40)
                .status(BusStatus.AVAILABLE)
                .build();

        BusDto.BusUpdateRequest update = new BusDto.BusUpdateRequest(
                45,
                BusStatus.IN_MAINTENANCE,
                "XYZ-789",
                new HashSet<>()
        );

        mapper.updateEntity(update, bus);

        assertThat(bus.getCapacity()).isEqualTo(45);
        assertThat(bus.getStatus()).isEqualTo(BusStatus.IN_MAINTENANCE);
        assertThat(bus.getPlate()).isEqualTo("XYZ-789");
    }
}
