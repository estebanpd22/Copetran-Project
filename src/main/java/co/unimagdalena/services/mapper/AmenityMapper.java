package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.domine.entities.Amenity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AmenityMapper {

    @Mapping(target = "id", ignore = true)
    Amenity toEntity(AmenityCreateRequest req);

    AmenityResponse toResponse(Amenity a);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(AmenityUpdateRequest dto, @MappingTarget Amenity amenity);
}
