package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.SeatDto;
import co.unimagdalena.domine.entities.Bus;
import co.unimagdalena.domine.entities.Seat;
import co.unimagdalena.domine.entities.SeatStatus;
import co.unimagdalena.domine.entities.SeatType;
import co.unimagdalena.services.mapper.SeatMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatMapperTest {
    private final SeatMapper mapper = Mappers.getMapper(SeatMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        SeatDto.SeatCreateRequest request = new SeatDto.SeatCreateRequest(
                new BigDecimal("45.00"),
                15,
                SeatType.PREFERENTIAL,
                SeatStatus.AVAILABLE,
                1L
        );

        Seat seat = mapper.toEntity(request);

        assertThat(seat.getPrice()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(seat.getNumber()).isEqualTo(15);
        assertThat(seat.getType()).isEqualTo(SeatType.PREFERENTIAL);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Bus bus = Bus.builder().id(1L).plate("ABC-123").build();

        Seat seat = Seat.builder()
                .id(1L)
                .price(new BigDecimal("45.00"))
                .number(15)
                .type(SeatType.STANDARD)
                .status(SeatStatus.AVAILABLE)
                .bus(bus)
                .build();

        SeatDto.SeatResponse dto = mapper.toResponse(seat);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(dto.number()).isEqualTo(15);
        assertThat(dto.type()).isEqualTo(SeatType.STANDARD);
        assertThat(dto.status()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void toSummary_shouldMapEntity() {
        Seat seat = Seat.builder()
                .id(1L)
                .number(15)
                .type(SeatType.STANDARD)
                .price(new BigDecimal("45.00"))
                .status(SeatStatus.AVAILABLE)
                .build();

        SeatDto.SeatSummary dto = mapper.toSummary(seat);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.number()).isEqualTo(15);
        assertThat(dto.type()).isEqualTo(SeatType.STANDARD);
        assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("45.00"));
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Seat seat = Seat.builder()
                .id(1L)
                .price(new BigDecimal("45.00"))
                .status(SeatStatus.AVAILABLE)
                .build();

        SeatDto.SeatUpdateRequest update = new SeatDto.SeatUpdateRequest(
                new BigDecimal("50.00"),
                null,
                null,
                SeatStatus.UNAVAILABLE,
                null
        );

        mapper.updateEntity(update, seat);

        assertThat(seat.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.UNAVAILABLE);
    }
}
