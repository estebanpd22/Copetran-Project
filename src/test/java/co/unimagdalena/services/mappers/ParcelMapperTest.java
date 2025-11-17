package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.ParcelDto;
import co.unimagdalena.domine.entities.Parcel;
import co.unimagdalena.domine.entities.ParcelStatus;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.domine.entities.Trip;
import co.unimagdalena.services.mapper.ParcelMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ParcelMapperTest {
    private final ParcelMapper mapper = Mappers.getMapper(ParcelMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        ParcelDto.ParcelCreateRequest request = new ParcelDto.ParcelCreateRequest(
                "John Sender",
                "555-0001",
                "Jane Receiver",
                "555-0002",
                new BigDecimal("35.00"),
                ParcelStatus.IN_TRANSIT,
                null,
                "OTP123",
                1L,
                2L,
                1L
        );

        Parcel parcel = mapper.toEntity(request);

        assertThat(parcel.getSenderName()).isEqualTo("John Sender");
        assertThat(parcel.getSenderPhone()).isEqualTo("555-0001");
        assertThat(parcel.getReceiverName()).isEqualTo("Jane Receiver");
        assertThat(parcel.getReceiverPhone()).isEqualTo("555-0002");
        assertThat(parcel.getPrice()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Stop fromStop = Stop.builder().id(1L).name("Stop A").build();
        Stop toStop = Stop.builder().id(2L).name("Stop B").build();
        Trip trip = Trip.builder().id(1L).build();

        Parcel parcel = Parcel.builder()
                .id(1L)
                .code("PCL-001")
                .senderName("John Sender")
                .senderPhone("555-0001")
                .receiverName("Jane Receiver")
                .receiverPhone("555-0002")
                .price(new BigDecimal("35.00"))
                .status(ParcelStatus.IN_TRANSIT)
                .fromStop(fromStop)
                .toStop(toStop)
                .trip(trip)
                .build();

        ParcelDto.ParcelResponse dto = mapper.toResponse(parcel);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("PCL-001");
        assertThat(dto.senderName()).isEqualTo("John Sender");
        assertThat(dto.receiverName()).isEqualTo("Jane Receiver");
        assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(dto.status()).isEqualTo(ParcelStatus.IN_TRANSIT);
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Parcel parcel = Parcel.builder()
                .id(1L)
                .senderName("John Sender")
                .status(ParcelStatus.CREATED)
                .price(new BigDecimal("35.00"))
                .build();

        ParcelDto.ParcelUpdateRequest update = new ParcelDto.ParcelUpdateRequest(
                "John Updated",
                null,
                null,
                null,
                new BigDecimal("40.00"),
                ParcelStatus.IN_TRANSIT,
                null,
                null,
                null,
                null,
                null
        );

        mapper.updateEntity(update, parcel);

        assertThat(parcel.getSenderName()).isEqualTo("John Updated");
        assertThat(parcel.getPrice()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.IN_TRANSIT);
    }
}
