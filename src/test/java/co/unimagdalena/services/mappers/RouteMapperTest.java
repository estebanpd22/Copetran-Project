package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.RouteDto;
import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.services.mapper.RouteMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class RouteMapperTest {
    private final RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        RouteDto.RouteCreateRequest request = new RouteDto.RouteCreateRequest(
                "R001",
                "Route 1",
                "City A",
                "City B",
                150.5f,
                180.0f
        );

        Route route = mapper.toEntity(request);

        assertThat(route.getCode()).isEqualTo("R001");
        assertThat(route.getName()).isEqualTo("Route 1");
        assertThat(route.getOrigin()).isEqualTo("City A");
        assertThat(route.getDestination()).isEqualTo("City B");
        assertThat(route.getDistanceKm()).isEqualTo(150.5f);
        assertThat(route.getDurationMin()).isEqualTo(180.0f);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Route route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Route 1")
                .origin("City A")
                .destination("City B")
                .distanceKm(150.5f)
                .durationMin(180.0f)
                .build();

        RouteDto.RouteResponse dto = mapper.toResponse(route);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("R001");
        assertThat(dto.name()).isEqualTo("Route 1");
        assertThat(dto.origin()).isEqualTo("City A");
        assertThat(dto.destination()).isEqualTo("City B");
    }

    @Test
    void toSummary_shouldMapEntity() {
        Route route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Route 1")
                .build();

        RouteDto.RouteSummary dto = mapper.toSummary(route);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("R001");
        assertThat(dto.name()).isEqualTo("Route 1");
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        Route route = Route.builder()
                .id(1L)
                .code("R001")
                .name("Route 1")
                .distanceKm(150.5f)
                .build();

        RouteDto.RouteUpdateRequest update = new RouteDto.RouteUpdateRequest(
                "R002",
                "Route 2",
                null,
                null,
                200.0f,
                null
        );

        mapper.updateEntity(update, route);

        assertThat(route.getCode()).isEqualTo("R002");
        assertThat(route.getName()).isEqualTo("Route 2");
        assertThat(route.getDistanceKm()).isEqualTo(200.0f);
    }
}
