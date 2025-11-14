package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    Optional<Amenity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}

