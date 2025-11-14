package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.domine.repositories.StopRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.StopService;
import co.unimagdalena.services.mapper.StopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StopServiceImpl implements StopService {

    private final StopRepository stopRepository;
    private final StopMapper stopMapper;

    @Override
    @Transactional
    public StopResponse createStop(StopCreateRequest request) {
        Stop stop = stopMapper.toEntity(request);

        // El 'stopOrder' debería ser NULL o 0 si se crea fuera de una ruta.
        if (stop.getRoute() == null && stop.getOrder() != null) {
            stop.setOrder(null);
        }

        Stop savedStop = stopRepository.save(stop);
        log.info("Parada creada con ID: {}", savedStop.getId());

        return stopMapper.toResponse(savedStop);
    }

    @Override
    @Transactional(readOnly = true)
    public StopResponse getStopById(Long stopId) {
        Stop stop = findStopById(stopId);
        return stopMapper.toResponse(stop);
    }

    @Override
    @Transactional
    public StopResponse updateStop(Long stopId, StopUpdateRequest request) {
        Stop stop = findStopById(stopId);

        stopMapper.updateEntity(request, stop);
        Stop updatedStop = stopRepository.save(stop);
        log.info("Parada ID {} actualizada.", stopId);

        return stopMapper.toResponse(updatedStop);
    }

    @Override
    @Transactional
    public void deleteStop(Long stopId) {
        Stop stop = findStopById(stopId);

        // Se asume que FareRules usan 'orphanRemoval=true' y la eliminación en cascada de los FareRules
        // ya se maneja por la configuración de la entidad.

        if (!stop.getFareRulesFrom().isEmpty() || !stop.getFareRulesTo().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la parada ID " + stopId + " porque tiene reglas de tarifa asociadas.");
        }

        stopRepository.delete(stop);
        log.info("Parada ID {} eliminada.", stopId);
    }

    // --- BÚSQUEDAS AVANZADAS ---

    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> getStopsByCity(String city) {

        log.warn("El filtro por ciudad '{}' se realiza en memoria, buscando coincidencias en el nombre de la parada.", city);

        return stopRepository.findAll().stream()
                .filter(stop -> stop.getName().toLowerCase().contains(city.toLowerCase()))
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> findStopsNearLocation(double latitude, double longitude, double radiusKm) {

        log.info("Buscando paradas dentro de {} km de [{}, {}].", radiusKm, latitude, longitude);

        final double R = 6371.0; // Radio de la Tierra en Km

        return stopRepository.findAll().stream()
                .filter(stop -> {
                    double latDistance = Math.toRadians(stop.getLatitude() - latitude);
                    double lonDistance = Math.toRadians(stop.getLongitude() - longitude);

                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                            + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(stop.getLatitude()))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double distance = R * c; // Distancia en km

                    return distance <= radiusKm;
                })
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StopResponse> getAllActiveStops() {

        return stopRepository.findAll().stream()
                .map(stopMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Stop findStopById(Long id) {
        return stopRepository.findStopById(id)
                .orElseThrow(() -> new NotFoundException("Parada no encontrada con ID: " + id));
    }
}