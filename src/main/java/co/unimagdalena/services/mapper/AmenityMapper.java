package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.domine.entities.Amenity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AmenityMapper {

    Amenity toEntity(AmenityCreateRequest req);

    AmenityResponse toResponse(Amenity a);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AmenityUpdateRequest dto, @MappingTarget Amenity amenity);
}
