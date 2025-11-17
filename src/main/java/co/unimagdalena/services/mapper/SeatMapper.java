package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.SeatDto.*;
import co.unimagdalena.domine.entities.Seat;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    Seat toEntity(SeatCreateRequest req);

    SeatResponse toResponse(Seat s);

    SeatSummary toSummary(Seat s);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SeatUpdateRequest req, @MappingTarget Seat entity);
}
