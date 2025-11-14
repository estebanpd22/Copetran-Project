package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.PassengerDto.*;
import co.unimagdalena.domine.entities.Passenger;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "user", ignore = true)
    Passenger toEntity(PassengerCreateRequest request);

    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "documentType", target = "documentType")
    @Mapping(source = "documentNumber", target = "documentNumber")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    PassengerResponse toResponse(Passenger passenger);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "user", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PassengerUpdateRequest request, @MappingTarget Passenger passenger);
}