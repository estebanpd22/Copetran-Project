package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.domine.entities.Incident;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Incident toEntity(IncidentCreateRequest req);

    IncidentResponse toResponse(Incident i);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "entityType", ignore = true)
    @Mapping(target = "entityId", ignore = true)
    void updateEntity(IncidentUpdateRequest req, @MappingTarget Incident entity);
}
