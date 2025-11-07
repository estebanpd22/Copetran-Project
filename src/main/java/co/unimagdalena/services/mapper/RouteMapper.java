package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.domine.entities.Route;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {StopMapper.class, FareRuleMapper.class})
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stops", ignore = true)
    @Mapping(target = "trips", ignore = true)
    @Mapping(target = "fareRules", ignore = true)
    Route toEntity(RouteCreateRequest req);

    RouteResponse toResponse(Route r);
    RouteSummary toSummary(Route r);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stops", ignore = true)
    @Mapping(target = "trips", ignore = true)
    @Mapping(target = "fareRules", ignore = true)
    void updateEntity(RouteUpdateRequest req, @MappingTarget Route entity);
}
