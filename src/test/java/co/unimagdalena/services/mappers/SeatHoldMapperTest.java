package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.SeatHoldDto;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.services.mapper.SeatHoldMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatHoldMapperTest {
    private final SeatHoldMapper mapper = Mappers.getMapper(SeatHoldMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(15);

        SeatHoldDto.SeatHoldCreateRequest request = new SeatHoldDto.SeatHoldCreateRequest(
                "A15",
                expiresAt,
                SeatHoldStatus.HOLD,
                1L,
                1L,
                1L
        );

        SeatHold seatHold = mapper.toEntity(request);

        assertThat(seatHold.getSeatNumber()).isEqualTo("A15");
        assertThat(seatHold.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(seatHold.getStatus()).isEqualTo(SeatHoldStatus.HOLD);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Seat seat = Seat.builder().id(1L).number(15).build();
        Trip trip = Trip.builder().id(1L).build();
        User user = User.builder().id(1L).fullName("John Doe").build();

        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(15);

        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .seatNumber("A15")
                .expiresAt(expiresAt)
                .status(SeatHoldStatus.HOLD)
                .seat(seat)
                .trip(trip)
                .user(user)
                .build();

        SeatHoldDto.SeatHoldResponse dto = mapper.toResponse(seatHold);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.seatNumber()).isEqualTo("A15");
        assertThat(dto.status()).isEqualTo(SeatHoldStatus.HOLD);
        assertThat(dto.seat()).isNotNull();
        assertThat(dto.trip()).isNotNull();
        assertThat(dto.user()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        SeatHold seatHold = SeatHold.builder()
                .id(1L)
                .seatNumber("A15")
                .status(SeatHoldStatus.HOLD)
                .build();

        OffsetDateTime newExpiry = OffsetDateTime.now().plusMinutes(30);

        SeatHoldDto.SeatHoldUpdateRequest update = new SeatHoldDto.SeatHoldUpdateRequest(
                "B20",
                newExpiry,
                SeatHoldStatus.EXPIRED,
                null,
                null,
                null
        );

        mapper.updateEntity(update, seatHold);

        assertThat(seatHold.getSeatNumber()).isEqualTo("B20");
        assertThat(seatHold.getExpiresAt()).isEqualTo(newExpiry);
        assertThat(seatHold.getStatus()).isEqualTo(SeatHoldStatus.EXPIRED);
    }
}
