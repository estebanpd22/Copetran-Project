package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.domine.entities.FareRule;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    FareRule toEntity(FareRuleCreateRequest req);

    FareRuleResponse toResponse(FareRule f);

    void updateEntity(FareRuleUpdateRequest req, @MappingTarget FareRule entity);
}