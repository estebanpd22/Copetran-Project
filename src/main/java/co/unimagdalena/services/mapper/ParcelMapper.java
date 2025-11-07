package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.ParcelDto.*;
import co.unimagdalena.domine.entities.Parcel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deliveryOTP", ignore = true)
    @Mapping(target = "proofPhotoUrl", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    Parcel toEntity(ParcelCreateRequest req);

    @Mapping(source = "trip.id", target = "trip.id")
    @Mapping(source = "trip.departureAt", target = "trip.departureAt")
    @Mapping(source = "trip.route.code", target = "trip.route.code")
    @Mapping(source = "fromStop.id", target = "fromStop.id")
    @Mapping(source = "fromStop.name", target = "fromStop.name")
    @Mapping(source = "fromStop.order", target = "fromStop.Order")
    @Mapping(source = "toStop.id", target = "toStop.id")
    @Mapping(source = "toStop.name", target = "toStop.name")
    @Mapping(source = "toStop.order", target = "toStop.Order")
    ParcelResponse toResponse(Parcel p);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    void updateEntity(ParcelUpdateRequest req, @MappingTarget Parcel entity);
}
