package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.domine.entities.Seat;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    Seat toEntity(SeatCreateRequest req);

    @Mapping(source = "bus.id", target = "bus.id")
    @Mapping(source = "bus.plate", target = "bus.plate")
    @Mapping(source = "bus.capacity", target = "bus.capacity")
    @Mapping(source = "bus.status", target = "bus.status")
    SeatResponse toResponse(Seat s);

    SeatSummary toSummary(Seat s);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    void updateEntity(SeatUpdateRequest req, @MappingTarget Seat entity);
}
