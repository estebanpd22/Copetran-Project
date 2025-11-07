package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.ConfigDto.*;
import co.unimagdalena.domine.entities.Config;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    @Mapping(target = "id", ignore = true)
    Config toEntity(ConfigCreateRequest req);

    ConfigResponse toResponse(Config c);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "key", ignore = true)
    void updateEntity(ConfigUpdateRequest req, @MappingTarget Config entity);
}
