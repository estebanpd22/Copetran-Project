package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.domine.entities.Purchase;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    Purchase toEntity(PurchaseCreateRequest req);

    PurchaseResponse toResponse(Purchase p);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PurchaseUpdateRequest req, @MappingTarget Purchase entity);
}
