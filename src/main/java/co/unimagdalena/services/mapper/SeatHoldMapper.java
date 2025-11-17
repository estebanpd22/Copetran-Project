package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.SeatHoldDto.*;
import co.unimagdalena.domine.entities.SeatHold;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    SeatHold toEntity(SeatHoldCreateRequest req);

    SeatHoldResponse toResponse(SeatHold sh);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SeatHoldUpdateRequest req, @MappingTarget SeatHold entity);
}