package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.domine.entities.Trip;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TripMapper {

    Trip toEntity(TripCreateRequest req);

    TripResponse toResponse(Trip t);

    TripSummary toSummary(Trip t);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TripUpdateRequest req, @MappingTarget Trip entity);
}
