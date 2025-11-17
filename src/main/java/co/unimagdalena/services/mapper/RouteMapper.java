package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.RouteDto.*;
import co.unimagdalena.domine.entities.Route;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {StopMapper.class, FareRuleMapper.class})
public interface RouteMapper {

    Route toEntity(RouteCreateRequest req);

    RouteResponse toResponse(Route r);
    RouteSummary toSummary(Route r);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RouteUpdateRequest req, @MappingTarget Route entity);
}
