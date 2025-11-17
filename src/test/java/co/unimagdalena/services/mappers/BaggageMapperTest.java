package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.BaggageDto;
import co.unimagdalena.domine.entities.Baggage;
import co.unimagdalena.domine.entities.Ticket;
import co.unimagdalena.services.mapper.BaggageMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class BaggageMapperTest {
    private final BaggageMapper mapper = Mappers.getMapper(BaggageMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        BaggageDto.BaggageCreateRequest request = new BaggageDto.BaggageCreateRequest(
                15.5f,
                new BigDecimal("25.00"),
                "TAG-001",
                1L
        );

        Baggage baggage = mapper.toEntity(request);

        assertThat(baggage.getWeightKg()).isEqualTo(15.5f);
        assertThat(baggage.getFee()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(baggage.getTagCode()).isEqualTo("TAG-001");
    }

    @Test
    void toResponse_shouldMapEntity() {
        Ticket ticket = Ticket.builder().id(1L).qrCode("QR-001").build();

        Baggage baggage = Baggage.builder()
                .id(1L)
                .weightKg(15.5f)
                .fee(new BigDecimal("25.00"))
                .tagCode("TAG-001")
                .ticket(ticket)
                .build();

        BaggageDto.BaggageResponse dto = mapper.toResponse(baggage);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.weightKg()).isEqualTo(15.5f);
        assertThat(dto.fee()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(dto.tagCode()).isEqualTo("TAG-001");
        assertThat(dto.ticket()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Baggage baggage = Baggage.builder()
                .id(1L)
                .weightKg(15.5f)
                .fee(new BigDecimal("25.00"))
                .tagCode("TAG-001")
                .build();

        BaggageDto.BaggageUpdateRequest update = new BaggageDto.BaggageUpdateRequest(
                20.0f,
                new BigDecimal("35.00"),
                "TAG-002",
                null
        );

        mapper.updateEntity(update, baggage);

        assertThat(baggage.getWeightKg()).isEqualTo(20.0f);
        assertThat(baggage.getFee()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(baggage.getTagCode()).isEqualTo("TAG-002");
    }
}
