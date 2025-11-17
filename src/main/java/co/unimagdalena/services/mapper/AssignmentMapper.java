package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.AssignmentDto.*;
import co.unimagdalena.domine.entities.Assignment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    Assignment toEntity(AssignmentCreateRequest request);

    AssignmentResponse toResponse(Assignment assignment);

    void updateEntity(AssignmentUpdateRequest request, @MappingTarget Assignment assignment);
}
