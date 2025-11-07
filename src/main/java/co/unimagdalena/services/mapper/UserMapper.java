package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.UserDto.*;
import co.unimagdalena.domine.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(UserCreateRequest req);

    UserResponse toResponse(User u);

    UserSummary toSummary(User u);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntity(UserUpdateRequest req, @MappingTarget User entity);
}