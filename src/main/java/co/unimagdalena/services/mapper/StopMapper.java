
package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.domine.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StopMapper {

    @Mapping(source= "stopOrder", target = "order")
    Stop toEntity(StopCreateRequest req);

    @Mapping(target= "stopOrder", source = "order")
    StopResponse toResponse(Stop s);

    @Mapping(target= "stopOrder", source = "order")
    StopSummary toSummary(Stop s);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source= "stopOrder", target = "order")
    void updateEntity(StopUpdateRequest req, @MappingTarget Stop entity);
}
