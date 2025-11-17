package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.Ticket;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    Ticket toEntity(TicketCreateRequest req);

    TicketResponse toResponse(Ticket t);

    TicketSummary toSummary(Ticket t);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TicketUpdateRequest req, @MappingTarget Ticket entity);
}
