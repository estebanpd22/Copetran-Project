package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.ParcelDto.*;
import co.unimagdalena.domine.entities.Parcel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    Parcel toEntity(ParcelCreateRequest req);

    ParcelResponse toResponse(Parcel p);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ParcelUpdateRequest req, @MappingTarget Parcel entity);
}
