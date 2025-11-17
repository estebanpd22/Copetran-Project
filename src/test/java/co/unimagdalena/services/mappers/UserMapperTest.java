package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.UserDto;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.entities.UserRole;
import co.unimagdalena.domine.entities.UserStatus;
import co.unimagdalena.services.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        UserDto.UserCreateRequest request = new UserDto.UserCreateRequest(
                "John Doe",
                "john@example.com",
                "555-0123",
                UserRole.PASSENGER,
                "password123"
        );

        User user = mapper.toEntity(request);

        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPhone()).isEqualTo("555-0123");
        assertThat(user.getRole()).isEqualTo(UserRole.PASSENGER);
    }

    @Test
    void toResponse_shouldMapEntity() {
        User user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("555-0123")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .build();

        UserDto.UserResponse dto = mapper.toResponse(user);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.fullName()).isEqualTo("John Doe");
        assertThat(dto.email()).isEqualTo("john@example.com");
        assertThat(dto.phone()).isEqualTo("555-0123");
        assertThat(dto.role()).isEqualTo(UserRole.PASSENGER);
        assertThat(dto.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void toSummary_shouldMapEntity() {
        User user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("555-0123")
                .build();

        UserDto.UserSummary dto = mapper.toSummary(user);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.fullName()).isEqualTo("John Doe");
        assertThat(dto.email()).isEqualTo("john@example.com");
        assertThat(dto.phone()).isEqualTo("555-0123");
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        User user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("555-0123")
                .role(UserRole.PASSENGER)
                .build();

        UserDto.UserUpdateRequest update = new UserDto.UserUpdateRequest(
                "Jane Doe",
                "jane@example.com",
                "555-9999",
                UserRole.DRIVER,
                null
        );

        mapper.updateEntity(update, user);

        assertThat(user.getFullName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getPhone()).isEqualTo("555-9999");
        assertThat(user.getRole()).isEqualTo(UserRole.DRIVER);
    }
}
