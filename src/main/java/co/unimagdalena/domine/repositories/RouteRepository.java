package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Long> {
    Optional<Route> findRouteById(Long id);
    Optional<Route> findByCode(String code);
    List<Route> findRoutesByName(String name);
    List<Route> findByOriginAndDestination(String origin, String destination);

    //Obtener la rutas ordenadas de menor a mayor cantidad de kilometros, indicando el origen y destino,
    List<Route> findRoutesByOriginAndDestinationOrderByDistanceKmAsc(String origin, String destination);

    //Obtener las rutas ordenadas de menor a mayor duracion en minutos, indicando el origen y destino
    List<Route> findRoutesByOriginAndDestinationOrderByDurationMinAsc(String origin, String destination);
}
