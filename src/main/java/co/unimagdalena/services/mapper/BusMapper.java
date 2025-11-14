package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.BusDto.*;
import co.unimagdalena.domine.entities.*;
import org.mapstruct.*;


@Mapper(componentModel = "spring", uses = AmenityMapper.class)
public interface BusMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trips", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(source = "status", target = "Status")
    Bus toEntity(BusCreateRequest req);

    @Mapping(source = "status", target = "Status")
    @Mapping(target = "totalSeats", expression = "java(bus.getSeats() != null ? bus.getSeats().size() : 0)")
    BusResponse toResponse(Bus bus);

    BusSummary toSummary(Bus bus);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trips", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(source = "status", target = "Status")
    void updateEntity(BusUpdateRequest req, @MappingTarget Bus bus);
}