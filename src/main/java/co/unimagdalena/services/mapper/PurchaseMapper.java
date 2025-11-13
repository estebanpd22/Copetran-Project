package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.domine.entities.Purchase;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {TicketMapper.class, UserMapper.class})
public interface PurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Purchase toEntity(PurchaseCreateRequest req);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "tickets", target = "tickets")
    PurchaseResponse toResponse(Purchase p);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    void updateEntity(PurchaseUpdateRequest req, @MappingTarget Purchase entity);
}
