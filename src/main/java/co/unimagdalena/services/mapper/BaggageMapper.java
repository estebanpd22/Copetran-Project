package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.domine.entities.Baggage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BaggageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    Baggage toEntity(BaggageCreateRequest req);

    @Mapping(source = "ticket.id", target = "ticket.id")
    @Mapping(source = "ticket.price", target = "ticket.price")
    @Mapping(source = "ticket.status", target = "ticket.status")
    @Mapping(source = "ticket.qrCode", target = "ticket.qrCode")
    @Mapping(source = "ticket.trip.id", target = "ticket.trip.id")
    @Mapping(source = "ticket.trip.departureAt", target = "ticket.trip.departureAt")
    BaggageResponse toResponse(Baggage b);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    void updateEntity(BaggageUpdateRequest req, @MappingTarget Baggage entity);
}