package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.domine.entities.Amenity;
import co.unimagdalena.domine.repositories.AmenityRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.mapper.AmenityMapper;
import co.unimagdalena.services.mapper.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository repository;
    private final AmenityMapper mapper;

    @Override
    public AmenityResponse createAmenity(AmenityCreateRequest request) {

        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalStateException("Amenity with name '" + request.name() + "' already exists.");
        }

        Amenity entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public AmenityResponse updateAmenity(Long id, AmenityUpdateRequest request) {

        Amenity amenity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Amenity with id " + id + " not found"));

        mapper.updateEntity(request, amenity);
        return mapper.toResponse(amenity);
    }

    @Override
    public void deleteAmenity(Long id) {

        if (!repository.existsById(id)) {
            throw new NotFoundException("Amenity with id " + id + " not found");
        }

        repository.deleteById(id);
    }

    @Override
    public AmenityResponse getAmenityById(Long id) {

        Amenity amenity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Amenity with id " + id + " not found"));

        return mapper.toResponse(amenity);
    }

    @Override
    public List<AmenityResponse> getAllAmenities() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

