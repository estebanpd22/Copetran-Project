package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.AmenityDto;
import co.unimagdalena.domine.entities.Amenity;
import co.unimagdalena.services.mapper.AmenityMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class AmenityMapperTest {
    private final AmenityMapper mapper = Mappers.getMapper(AmenityMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        Amenity amenity = mapper.toEntity(new AmenityDto.AmenityCreateRequest("Wifi"));
        assertThat(amenity.getName()).isEqualTo("Wifi");
    }

    @Test
    void toResponse_shouldMapEntity() {
        var amenity = Amenity.builder().id(5L).name("Wifi").build();
        AmenityDto.AmenityResponse dto = mapper.toResponse(amenity);
        assertThat(amenity.getId()).isNotNull();
        assertThat(amenity.getName()).isEqualTo("Wifi");
    }

    @Test
    void updateEntity_shouldUpdateName() {
        var amenity = Amenity.builder().id(5L).name("Wifi").build();
        var update = new AmenityDto.AmenityUpdateRequest("Bathrom");
        mapper.updateEntity(update, amenity);
        assertThat(amenity.getName()).isEqualTo("Bathrom");
    }
}
