package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.domine.entities.Assignment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "dispatcher", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    Assignment toEntity(AssignmentCreateRequest request);

    @Mapping(source = "trip.id", target = "trip.id")
    @Mapping(source = "trip.departureAt", target = "trip.departureAt")
    @Mapping(source = "trip.route.code", target = "trip.route.code")
    @Mapping(source = "trip.route.name", target = "trip.route.name")
    @Mapping(source = "trip.route.origin", target = "trip.route.origin")
    @Mapping(source = "trip.route.destination", target = "trip.route.destination") // Destino
    @Mapping(source = "driver.id", target = "driver.id")
    @Mapping(source = "driver.fullName", target = "driver.fullName")
    @Mapping(source = "dispatcher.id", target = "dispatcher.id")
    @Mapping(source = "dispatcher.fullName", target = "dispatcher.fullName")
    AssignmentResponse toResponse(Assignment assignment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "dispatcher", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(AssignmentUpdateRequest request, @MappingTarget Assignment assignment);
}
