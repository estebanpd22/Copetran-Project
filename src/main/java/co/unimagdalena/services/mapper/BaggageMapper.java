package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.domine.entities.Baggage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BaggageMapper {

    Baggage toEntity(BaggageCreateRequest req);

    BaggageResponse toResponse(Baggage b);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BaggageUpdateRequest req, @MappingTarget Baggage entity);
}