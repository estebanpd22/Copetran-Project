package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.Ticket;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    Ticket toEntity(TicketCreateRequest req);

    @Mapping(source = "trip.id", target = "trip.id")
    @Mapping(source = "trip.departureAt", target = "trip.departureAt")
    @Mapping(source = "trip.route.code", target = "trip.route.code")
    @Mapping(source = "passenger.id", target = "passenger.id")
    @Mapping(source = "passenger.fullName", target = "passenger.fullName")
    @Mapping(source = "seat.id", target = "seat.id")
    @Mapping(source = "seat.number", target = "seat.number")
    @Mapping(source = "seat.type", target = "seat.type")
    @Mapping(source = "fromStop.id", target = "fromStop.id")
    @Mapping(source = "fromStop.name", target = "fromStop.name")
    @Mapping(source = "fromStop.order", target = "fromStop.Order")
    @Mapping(source = "toStop.id", target = "toStop.id")
    @Mapping(source = "toStop.name", target = "toStop.name")
    @Mapping(source = "toStop.order", target = "toStop.Order")
    TicketResponse toResponse(Ticket t);

    TicketSummary toSummary(Ticket t);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "passenger", ignore = true)
    @Mapping(target = "seat", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    void updateEntity(TicketUpdateRequest req, @MappingTarget Ticket entity);
}
