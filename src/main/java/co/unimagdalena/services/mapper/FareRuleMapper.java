package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.domine.entities.FareRule;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    FareRule toEntity(FareRuleCreateRequest req);

    @Mapping(source = "route.id", target = "route.id")
    @Mapping(source = "route.code", target = "route.code")
    @Mapping(source = "route.name", target = "route.name")
    @Mapping(source = "fromStop.id", target = "fromStop.id")
    @Mapping(source = "fromStop.name", target = "fromStop.name")
    @Mapping(source = "fromStop.order", target = "fromStop.Order")
    @Mapping(source = "toStop.id", target = "toStop.id")
    @Mapping(source = "toStop.name", target = "toStop.name")
    @Mapping(source = "toStop.order", target = "toStop.Order")
    FareRuleResponse toResponse(FareRule f);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    void updateEntity(FareRuleUpdateRequest req, @MappingTarget FareRule entity);
}