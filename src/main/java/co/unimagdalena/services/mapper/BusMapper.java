package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.*;
import org.mapstruct.*;


@Mapper(componentModel = "spring", uses = AmenityMapper.class)
public interface BusMapper {

    Bus toEntity(BusCreateRequest req);

    BusResponse toResponse(Bus bus);

    BusSummary toSummary(Bus bus);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BusUpdateRequest req, @MappingTarget Bus bus);
}