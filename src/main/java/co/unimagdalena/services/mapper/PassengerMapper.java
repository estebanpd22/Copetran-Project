package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.PassengerDto.*;
import co.unimagdalena.domine.entities.Passenger;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    Passenger toEntity(PassengerCreateRequest request);

    PassengerResponse toResponse(Passenger passenger);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(PassengerUpdateRequest request, @MappingTarget Passenger passenger);
}