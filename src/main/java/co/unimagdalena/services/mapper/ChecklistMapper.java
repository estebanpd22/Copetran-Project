package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.ChecklistDto.*;
import co.unimagdalena.domine.entities.Checklist;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ChecklistMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "completed", constant = "false")
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Checklist toEntity(ChecklistCreateRequest request);

    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "allChecksComplete", expression = "java(checklist.isAllChecksComplete())")
    @Mapping(target = "completedByUserId", source = "completedBy.id")
    @Mapping(target = "completedByUserName", expression = "java(checklist.getCompletedBy() != null ? checklist.getCompletedBy().getFullName() : null)")
    ChecklistResponse toResponse(Checklist checklist);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    void updateEntity(ChecklistUpdateRequest request, @MappingTarget Checklist checklist);
}
