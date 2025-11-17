package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.PassengerDto;
import co.unimagdalena.domine.entities.Passenger;
import co.unimagdalena.services.mapper.PassengerMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class PassengerMapperTest {
    private final PassengerMapper mapper = Mappers.getMapper(PassengerMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        PassengerDto.PassengerCreateRequest request = new PassengerDto.PassengerCreateRequest(
                "John Doe",
                "CC",
                "123456789",
                LocalDate.of(1990, 5, 15),
                "555-0123",
                null
        );

        Passenger passenger = mapper.toEntity(request);

        assertThat(passenger.getFullName()).isEqualTo("John Doe");
        assertThat(passenger.getDocumentType()).isEqualTo("CC");
        assertThat(passenger.getDocumentNumber()).isEqualTo("123456789");
        assertThat(passenger.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(passenger.getPhoneNumber()).isEqualTo("555-0123");
    }

    @Test
    void toResponse_shouldMapEntity() {
        Passenger passenger = Passenger.builder()
                .id(1L)
                .fullName("John Doe")
                .documentType("CC")
                .documentNumber("123456789")
                .birthDate(LocalDate.of(1990, 5, 15))
                .phoneNumber("555-0123")
                .build();

        PassengerDto.PassengerResponse dto = mapper.toResponse(passenger);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.fullName()).isEqualTo("John Doe");
        assertThat(dto.documentType()).isEqualTo("CC");
        assertThat(dto.documentNumber()).isEqualTo("123456789");
        assertThat(dto.birthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(dto.phoneNumber()).isEqualTo("555-0123");
    }

    @Test
    void updateEntityFromRequest_shouldUpdateFields() {
        Passenger passenger = Passenger.builder()
                .id(1L)
                .fullName("John Doe")
                .documentType("CC")
                .documentNumber("123456789")
                .phoneNumber("555-0123")
                .build();

        PassengerDto.PassengerUpdateRequest update = new PassengerDto.PassengerUpdateRequest(
                null,
                "Jane Doe",
                "CE",
                "987654321",
                null,
                "555-9999"
        );

        mapper.updateEntityFromRequest(update, passenger);

        assertThat(passenger.getFullName()).isEqualTo("Jane Doe");
        assertThat(passenger.getDocumentType()).isEqualTo("CE");
        assertThat(passenger.getDocumentNumber()).isEqualTo("987654321");
        assertThat(passenger.getPhoneNumber()).isEqualTo("555-9999");
    }
}
