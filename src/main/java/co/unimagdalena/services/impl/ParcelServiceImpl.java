package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.IncidentDto.*;
import co.unimagdalena.api.dto.ParcelDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.ParcelRepository;
import co.unimagdalena.domine.repositories.StopRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.IncidentService;
import co.unimagdalena.services.ParcelService;
import co.unimagdalena.services.mapper.ParcelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final IncidentService incidentService;
    private final ParcelMapper parcelMapper;

    private final Random random = new Random();

    @Override
    public ParcelResponse createParcel(ParcelCreateRequest request) {

        log.debug("Creando nuevo parcel...");

        // Generar código único del envío
        String code = generateUniqueParcelCode();
        log.debug("Código generado: {}", code);

        // Validar paradas de origen y destino
        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException(
                        "La parada de origen no existe: " + request.fromStopId()
                ));

        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException(
                        "La parada de destino no existe: " + request.toStopId()
                ));

        // Validar que ambas paradas pertenecen a la misma ruta
        if (!fromStop.getRoute().getId().equals(toStop.getRoute().getId())) {
            throw new IllegalArgumentException(
                    "Las paradas de origen y destino deben pertenecer a la misma ruta"
            );
        }

        // Validar orden correcto
        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalArgumentException(
                    "El orden de la parada de origen debe ser menor que el de destino"
            );
        }

        // Crear entidad
        Parcel parcel = parcelMapper.toEntity(request);
        parcel.setCode(code);
        parcel.setFromStop(fromStop);
        parcel.setToStop(toStop);
        parcel.setStatus(ParcelStatus.CREATED);

        // Si viene un tripId → validar y asignar
        if (request.tripId() != null) {
            Trip trip = tripRepository.findById(request.tripId())
                    .orElseThrow(() -> new NotFoundException(
                            "No existe el trip con ID " + request.tripId()
                    ));

            // Validar que el trip pertenece a la misma ruta
            if (!trip.getRoute().getId().equals(fromStop.getRoute().getId())) {
                throw new IllegalArgumentException(
                        "El trip debe pertenecer a la misma ruta que las paradas del parcel"
                );
            }

            // Validar que el trip no esté finalizado o cancelado
            if (trip.getStatus() == TripStatus.ARRIVED || trip.getStatus() == TripStatus.CANCELLED) {
                throw new IllegalStateException(
                        "No se puede asignar un parcel a un trip con estado: " + trip.getStatus()
                );
            }

            parcel.setTrip(trip);
            parcel.setStatus(ParcelStatus.IN_TRANSIT);
        }

        // Generar OTP de entrega
        String otp = generateOtp();
        parcel.setDeliveryOTP(otp);

        // Guardar
        Parcel saved = parcelRepository.save(parcel);

        log.info("Parcel creado correctamente. CODE={} OTP={}", saved.getCode(), otp);
        return parcelMapper.toResponse(saved);
    }

    @Override
    public void updateParcel(Long parcelId, ParcelUpdateRequest request) {

        log.debug("Actualizando parcel ID {}", parcelId);

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException(
                        "Parcel no encontrado con ID " + parcelId
                ));

        // Solo se puede actualizar cuando está recién creado
        if (parcel.getStatus() != ParcelStatus.CREATED) {
            throw new IllegalStateException(
                    "Solo se pueden actualizar parcels con estado CREATED. Estado actual: " + parcel.getStatus()
            );
        }

        // Actualizar campos simples
        parcelMapper.updateEntity(request, parcel);

        // Si se quiere actualizar paradas
        if (request.fromStopId() != null || request.toStopId() != null) {

            Long newFromId = request.fromStopId() != null ? request.fromStopId() : parcel.getFromStop().getId();
            Long newToId = request.toStopId() != null ? request.toStopId() : parcel.getToStop().getId();

            Stop newFrom = stopRepository.findById(newFromId)
                    .orElseThrow(() -> new NotFoundException("Stop no encontrada: " + newFromId));

            Stop newTo = stopRepository.findById(newToId)
                    .orElseThrow(() -> new NotFoundException("Stop no encontrada: " + newToId));

            // Validar ruta
            if (!newFrom.getRoute().getId().equals(newTo.getRoute().getId())) {
                throw new IllegalArgumentException("Las paradas deben pertenecer a la misma ruta");
            }

            // Validar orden correcto
            if (newFrom.getOrder() >= newTo.getOrder()) {
                throw new IllegalArgumentException("El orden de origen debe ser menor al de destino");
            }

            parcel.setFromStop(newFrom);
            parcel.setToStop(newTo);

        }

        parcelRepository.save(parcel);
        log.info("Parcel {} actualizado correctamente.", parcelId);
    }

    @Override
    public void assignTrip(Long parcelId, Long tripId) {

        log.debug("Asignando trip {} al parcel {}", tripId, parcelId);

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException(
                        "Parcel no encontrado con ID " + parcelId
                ));

        // Solo se puede asignar viaje si sigue creado
        if (parcel.getStatus() != ParcelStatus.CREATED) {
            throw new IllegalStateException(
                    "Solo se pueden asignar trips a parcels con estado CREATED"
            );
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException(
                        "Trip no encontrado con ID " + tripId
                ));

        // Validar que trip pertenece a la ruta del parcel
        if (!trip.getRoute().getId().equals(parcel.getFromStop().getRoute().getId())) {
            throw new IllegalArgumentException(
                    "El trip debe pertenecer a la misma ruta del parcel"
            );
        }

        // Validar estado del trip
        if (trip.getStatus() == TripStatus.ARRIVED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new IllegalStateException(
                    "No se puede asignar un parcel a un trip con estado " + trip.getStatus()
            );
        }

        parcel.setTrip(trip);
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        parcelRepository.save(parcel);

        log.info("Parcel {} asignado correctamente al trip {}", parcelId, tripId);
    }

    @Override
    @Transactional(readOnly = true)
    public ParcelResponse getParcelByCode(String code) {
        Parcel parcel = parcelRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(
                        "No existe ningún parcel con código " + code
                ));
        return parcelMapper.toResponse(parcel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParcelResponse> getParcelsByTrip(Long tripId) {

        if (!tripRepository.existsById(tripId)) {
            throw new NotFoundException("Trip no encontrado con ID " + tripId);
        }

        return parcelRepository.findByTripId(tripId)
                .stream()
                .map(parcelMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParcelResponse> getParcelsBySender(String senderPhone) {

        return parcelRepository.findBySenderPhone(senderPhone)
                .stream()
                .map(parcelMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParcelResponse> getParcelsByReceiver(String receiverPhone) {

        return parcelRepository.findByReceiverPhone(receiverPhone)
                .stream()
                .map(parcelMapper::toResponse)
                .toList();
    }

    @Override
    public void confirmDelivery(Long parcelId, String otp, String proofPhotoUrl) {

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException(
                        "Parcel no encontrado con ID " + parcelId
                ));

        if (parcel.getStatus() != ParcelStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Solo se pueden entregar parcels en estado IN_TRANSIT"
            );
        }

        // Validar OTP
        if (!parcel.getDeliveryOTP().equals(otp) && parcelRepository.findById(parcelId).get().getId().equals(parcelId)) {
            markDeliveryFailed(parcelId, "OTP incorrecto");
            return;
        }

        if (proofPhotoUrl == null || proofPhotoUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Se requiere una foto de prueba para confirmar la entrega"
            );
        }

        parcel.setStatus(ParcelStatus.DELIVERED);
        parcel.setProofPhotoUrl(proofPhotoUrl);
        parcelRepository.save(parcel);

        log.info("Entrega confirmada correctamente para parcel {}", parcelId);
    }

    // ---------------------------------------------------------------------------------------
    //                                    FALLÓ LA ENTREGA
    // ---------------------------------------------------------------------------------------

    @Override
    public void markDeliveryFailed(Long parcelId, String failureNote) {

        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException(
                        "Parcel no encontrado con ID " + parcelId
                ));

        if (parcel.getStatus() != ParcelStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Solo se pueden marcar fallidos parcels en estado IN_TRANSIT"
            );
        }

        parcel.setStatus(ParcelStatus.FAILED);
        parcelRepository.save(parcel);

        log.warn("Parcel {} marcado como FAILED. Motivo: {}", parcelId, failureNote);

        // Crear incidente automáticamente
        try {
            incidentService.createIncident(
                    new IncidentCreateRequest(
                            EntityType.PARCEL,
                            parcelId,
                            IncidentType.DELIVERY_FAIL,
                            failureNote
                    )
            );
        } catch (Exception ex) {
            log.error("Error al crear incidente para parcel {}: {}", parcelId, ex.getMessage());
        }
    }

    private String generateUniqueParcelCode() {
        String code;
        do {
            code = generateParcelCode();
        } while (parcelRepository.findByCode(code).isPresent());
        return code;
    }

    private String generateParcelCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = random.nextInt(10000);
        return "PAQ-%s-%04d".formatted(date, seq);
    }

    private String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }
}

