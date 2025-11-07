package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.domine.entities.Stop;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StopMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "fareRulesFrom", ignore = true)
    @Mapping(target = "fareRulesTo", ignore = true)
    Stop toEntity(StopCreateRequest req);

    StopResponse toResponse(Stop s);

    @Mapping(source = "order", target = "Order")
    StopSummary toSummary(Stop s);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "fareRulesFrom", ignore = true)
    @Mapping(target = "fareRulesTo", ignore = true)
    void updateEntity(StopUpdateRequest req, @MappingTarget Stop entity);
}
