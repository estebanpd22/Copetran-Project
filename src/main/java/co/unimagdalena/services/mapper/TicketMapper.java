package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.Ticket;
import org.mapstruct.*;

import javax.xml.transform.Source;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "paymentMethod", target = "purchase.paymentMethod")
    Ticket toEntity(TicketCreateRequest req);

    @Mapping(source = "purchase.paymentMethod", target = "paymentMethod")
    TicketResponse toResponse(Ticket t);

    TicketSummary toSummary(Ticket t);

    @Mapping(source = "paymentMethod", target = "purchase.paymentMethod")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TicketUpdateRequest req, @MappingTarget Ticket entity);
}
