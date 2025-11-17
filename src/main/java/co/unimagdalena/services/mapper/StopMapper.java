package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.domine.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StopMapper {

    Stop toEntity(StopCreateRequest req);

    StopResponse toResponse(Stop s);

    StopSummary toSummary(Stop s);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(StopUpdateRequest req, @MappingTarget Stop entity);
}
