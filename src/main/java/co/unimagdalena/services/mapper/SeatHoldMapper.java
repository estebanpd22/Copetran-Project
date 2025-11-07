package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.SeatHoldDto.*;
import co.unimagdalena.domine.entities.SeatHold;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    SeatHold toEntity(SeatHoldCreateRequest req);

    @Mapping(source = "seat.id", target = "seat.id")
    @Mapping(source = "seat.number", target = "seat.number")
    @Mapping(source = "seat.type", target = "seat.type")
    @Mapping(source = "trip.id", target = "trip.id")
    @Mapping(source = "trip.departureAt", target = "trip.departureAt")
    @Mapping(source = "trip.route.code", target = "trip.route.code")
    @Mapping(source = "user.id", target = "user.id")
    @Mapping(source = "user.fullName", target = "user.fullName")
    SeatHoldResponse toResponse(SeatHold sh);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(SeatHoldUpdateRequest req, @MappingTarget SeatHold entity);
}