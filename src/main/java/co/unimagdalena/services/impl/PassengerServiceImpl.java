package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.PassengerDto.*;
import co.unimagdalena.domine.entities.Passenger;
import co.unimagdalena.domine.entities.User;
import co.unimagdalena.domine.repositories.PassengerRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.PassengerService;
import co.unimagdalena.services.mapper.PassengerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository repository;
    private final PassengerMapper mapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PassengerResponse createPassenger(PassengerCreateRequest request) {

        // 1. Lógica de Negocio: Validar duplicados
        if (repository.existsByDocumentNumber(request.documentNumber())) {
            log.warn("Intento de crear pasajero con documento duplicado: {}", request.documentNumber());
            // Usamos IllegalStateException (mapea a 409 Conflict)
            throw new IllegalStateException(
                    String.format("Ya existe un pasajero con el documento %s", request.documentNumber())
            );
        }

        Passenger passenger = mapper.toEntity(request);

        // 2. Lógica de Servicio: Asociar usuario (manejando "invitados")
        if (request.userId() != null) {
            log.debug("Asociando pasajero con User ID: {}", request.userId());
            User user = userRepository.findUserById(request.userId())
                    .orElseThrow(() -> {
                        log.error("Usuario con ID {} no encontrado al crear pasajero", request.userId());
                        return new NotFoundException(String.format("Usuario con ID %d no encontrado", request.userId()));
                    });
            passenger.setUser(user);
        } else {
            log.debug("Creando pasajero sin usuario asociado (compra de invitado)");
        }

        passenger.setCreatedAt(OffsetDateTime.now());
        Passenger savedPassenger = repository.save(passenger);
        log.info("Pasajero creado con ID: {}", savedPassenger.getId());

        return mapper.toResponse(savedPassenger);
    }

    @Override
    @Transactional
    public void updatePassenger(Long id, PassengerUpdateRequest request) {
        // 1. Obtener Entidad
        Passenger passenger = repository.findPassengerById(id).orElseThrow(() -> {
            log.error("Pasajero no encontrado para actualizar. ID: {}", id);
            return new NotFoundException(String.format("Pasajero con ID %d no encontrado", id));
        });

        // 2. Mapeo (PATCH)
        mapper.updateEntityFromRequest(request, passenger);

        // 3. Lógica de Servicio: Re-asociar usuario
        if (request.userId() != null && (passenger.getUser() == null || !request.userId().equals(passenger.getUser().getId()))) {
            log.debug("Re-asociando pasajero ID {} con nuevo User ID {}", id, request.userId());
            User newUser = userRepository.findUserById(request.userId())
                    .orElseThrow(() -> new NotFoundException(String.format("Usuario con ID %d no encontrado", request.userId())));
            passenger.setUser(newUser);
        }

        repository.save(passenger);
        log.info("Pasajero actualizado. ID: {}", id);
    }

    @Override
    @Transactional
    public void deletePassenger(Long id) {
        if (!repository.existsById(id)) {
            log.error("Pasajero no encontrado para eliminar. ID: {}", id);
            throw new NotFoundException(String.format("Pasajero con ID %d no encontrado", id));
        }
        repository.deleteById(id);
        log.info("Pasajero eliminado. ID: {}", id);
    }

    // --- BÚSQUEDAS (Finders) ---

    @Override
    @Transactional(readOnly = true)
    public List<PassengerResponse> getPassengerByUser(Long userId) {
        List<Passenger> passengers = repository.findByUserId(userId);
        return passengers.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerResponse finByDocumentNumber(String documentNumber) {
        Passenger passenger = repository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> {
                    log.warn("Pasajero no encontrado por documento: {}", documentNumber);
                    return new NotFoundException("Pasajero no encontrado con el documento " + documentNumber);
                });
        return mapper.toResponse(passenger);
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerResponse getPassengerById(Long id) {
        Passenger passenger = repository.findPassengerById(id)
                .orElseThrow(() -> {
                    log.warn("Pasajero no encontrado por ID: {}", id);
                    return new NotFoundException(String.format("Pasajero con ID %d no encontrado", id));
                });
        return mapper.toResponse(passenger);
    }
}