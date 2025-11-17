package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.AssignmentDto;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.services.mapper.AssignmentMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignmentMapperTest {
    private final AssignmentMapper mapper = Mappers.getMapper(AssignmentMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        AssignmentDto.AssignmentCreateRequest request = new AssignmentDto.AssignmentCreateRequest(
                true,
                LocalDateTime.of(2025, 1, 15,15,20),
                1L,
                2L,
                3L
        );

        Assignment assignment = mapper.toEntity(request);

        assertThat(assignment.getAssignedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15,15,20));
    }

    @Test
    void toResponse_shouldMapEntity() {
        Route route = Route.builder().id(1L).code("R001").name("Route 1").build();
        Bus bus = Bus.builder().id(1L).plate("ABC123").build();
        Trip trip = Trip.builder()
                .id(1L)
                .date(LocalDate.of(2025, 1, 15))
                .status(TripStatus.SCHEDULED)
                .route(route)
                .bus(bus)
                .build();

        User driver = User.builder().id(2L).fullName("John Driver").email("driver@test.com").build();
        User dispatcher = User.builder().id(3L).fullName("Jane Dispatcher").email("dispatcher@test.com").build();

        Assignment assignment = Assignment.builder()
                .id(1L)
                .checkListOk(true)
                .assignedAt(LocalDateTime.of(2025, 1, 15,17,25))
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .build();

        AssignmentDto.AssignmentResponse dto = mapper.toResponse(assignment);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.checkListOk()).isTrue();
        assertThat(dto.assignedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15,17,25));
        assertThat(dto.trip()).isNotNull();
        assertThat(dto.driver()).isNotNull();
        assertThat(dto.dispatcher()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        LocalDateTime assignedAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAssignedAt = LocalDateTime.now().minusDays(1);
        var assignment = Assignment.builder()
                .id(1L)
                .checkListOk(true)
                .assignedAt(assignedAt)
                .build();

        AssignmentDto.AssignmentUpdateRequest update = new AssignmentDto.AssignmentUpdateRequest(
                false,
                updatedAssignedAt,
                null,
                null,
                null
        );

        mapper.updateEntity(update, assignment);

        assertThat(assignment.getAssignedAt()).isEqualTo(updatedAssignedAt);
    }
}
