package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.ConfigDto.*;
import co.unimagdalena.domine.entities.Config;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    Config toEntity(ConfigCreateRequest req);

    ConfigResponse toResponse(Config c);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ConfigUpdateRequest req, @MappingTarget Config entity);
}
