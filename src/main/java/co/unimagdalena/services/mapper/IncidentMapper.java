package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.domine.entities.Incident;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    Incident toEntity(IncidentCreateRequest req);

    IncidentResponse toResponse(Incident i);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(IncidentUpdateRequest req, @MappingTarget Incident entity);
}