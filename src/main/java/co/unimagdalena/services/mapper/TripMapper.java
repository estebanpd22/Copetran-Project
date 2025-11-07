package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.TripDto.*;
import co.unimagdalena.domine.entities.Trip;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    Trip toEntity(TripCreateRequest req);

    @Mapping(source = "route.id", target = "route.id")
    @Mapping(source = "route.code", target = "route.code")
    @Mapping(source = "route.name", target = "route.name")
    @Mapping(source = "bus.id", target = "bus.id")
    @Mapping(source = "bus.plate", target = "bus.plate")
    @Mapping(source = "bus.capacity", target = "bus.capacity")
    @Mapping(source = "bus.status", target = "bus.status")
    TripResponse toResponse(Trip t);

    @Mapping(source = "route.id", target = "route.id")
    @Mapping(source = "route.code", target = "route.code")
    @Mapping(source = "route.name", target = "route.name")
    TripSummary toSummary(Trip t);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    void updateEntity(TripUpdateRequest req, @MappingTarget Trip entity);
}
