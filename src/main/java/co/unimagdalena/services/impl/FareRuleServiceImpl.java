package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.FareRuleDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.*;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.FareRuleService;
import co.unimagdalena.services.mapper.FareRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository fareRuleRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final PassengerRepository passengerRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final BusRepository busRepository;
    private final FareRuleMapper mapper;

    @Override
    public FareRuleResponse createFareRule(FareRuleCreateRequest request) {

        // Buscar la ruta
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new NotFoundException("Ruta no encontrada con ID: " + request.routeId()));

        // Buscar paradas
        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException("Parada origen no encontrada con ID: " + request.fromStopId()));

        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException("Parada destino no encontrada con ID: " + request.toStopId()));

        // Validar que ambas paradas pertenecen a la ruta
        validateStopsBelongToRoute(route.getId(), fromStop.getId(), toStop.getId());

        // Validar orden correcto (origen < destino)
        validateStopOrder(fromStop, toStop);

        // Convertir DTO a entidad (MapStruct)
        FareRule rule = mapper.toEntity(request);
        rule.setRoute(route);
        rule.setFromStop(fromStop);
        rule.setToStop(toStop);

        fareRuleRepository.save(rule);

        return mapper.toResponse(rule);
    }

    @Override
    public void updateFareRule(Long id, FareRuleUpdateRequest request) {

        FareRule rule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule no encontrada con ID: " + id));

        // Actualización parcial
        mapper.updateEntity(request, rule);

        fareRuleRepository.save(rule);
    }

    @Override
    public void deleteFareRule(Long id) {

        FareRule rule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule no encontrada con ID: " + id));

        fareRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public FareRuleResponse getFareRule(Long id) {

        FareRule rule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule no encontrada con ID: " + id));

        return mapper.toResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareRuleResponse> getAllFareRules() {
        return fareRuleRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FareRuleResponse> getFareRulesByRouteId(Long routeId) {

        if (!routeRepository.existsById(routeId)) {
            throw new NotFoundException("Ruta no encontrada con ID: " + routeId);
        }

        return fareRuleRepository.findByRouteId(routeId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getFinalTicketPrice(Long routeId,
                                          Long fromStopId,
                                          Long toStopId,
                                          Long passengerId,
                                          Long busId,
                                          String seatNumber,
                                          Long tripId) {

        // 1. Obtener la regla correspondiente a la ruta y paradas
        FareRule rule = fareRuleRepository
                .findByRouteIdAndFromStopIdAndToStopId(routeId, fromStopId, toStopId)
                .orElseThrow(() -> new NotFoundException("No existe FareRule para los parámetros enviados."));

        BigDecimal basePrice = rule.getBasePrice();

        // 2. Obtener pasajero y calcular descuento según edad
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new NotFoundException("Pasajero no encontrado con ID: " + passengerId));

        BigDecimal ageDiscount = calculateAgeDiscount(passenger.getBirthDate(), rule);

        // 3. Recargo por asiento preferencial
        Seat seat = seatRepository.findByBusIdAndNumber(busId, Integer.valueOf(seatNumber))
                .orElseThrow(() -> new NotFoundException("Asiento no encontrado en el bus."));

        BigDecimal seatSurcharge = calculateSeatSurcharge(seat, basePrice);

        // 4. Aplicar descuento por edad
        BigDecimal priceAfterDiscount =
                basePrice.multiply(BigDecimal.ONE.subtract(ageDiscount));

        // 5. Recargo dinámico (si aplica)
        BigDecimal dynamicSurcharge = BigDecimal.ZERO;

        if (rule.getDynamicPricing() == DynamicPricing.ON) {
            dynamicSurcharge = calculateDynamicSurcharge(tripId, busId, basePrice);
        }

        // 6. Calcular precio final
        return priceAfterDiscount
                .add(seatSurcharge)
                .add(dynamicSurcharge);
    }

    // ============================================================
    //                  MÉTODOS AUXILIARES
    // ============================================================

    /** Valida que ambas paradas pertenezcan a la misma ruta */
    private void validateStopsBelongToRoute(Long routeId, Long fromStopId, Long toStopId) {

        Stop from = stopRepository.findById(fromStopId)
                .orElseThrow(() -> new NotFoundException("Parada origen no encontrada."));

        Stop to = stopRepository.findById(toStopId)
                .orElseThrow(() -> new NotFoundException("Parada destino no encontrada."));

        if (!from.getRoute().getId().equals(routeId)) {
            throw new IllegalArgumentException("La parada origen no pertenece a la ruta especificada.");
        }

        if (!to.getRoute().getId().equals(routeId)) {
            throw new IllegalArgumentException("La parada destino no pertenece a la ruta especificada.");
        }
    }

    /** Valida que el orden de las paradas sea correcto: origen < destino */
    private void validateStopOrder(Stop from, Stop to) {
        if (from.getOrder() >= to.getOrder()) {
            throw new IllegalArgumentException("El orden de la parada origen debe ser menor que el de la parada destino.");
        }
    }

    /** Calcula descuento por edad usando el mapa de descuentos de FareRule */
    private BigDecimal calculateAgeDiscount(LocalDate birthDate, FareRule rule) {

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        Map<String, Double> discounts = rule.getDiscounts();

        if (discounts == null || discounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Double percentage = null;

        if (age < 12) {
            percentage = discounts.get("child");
        } else if (age >= 60) {
            percentage = discounts.get("senior");
        } else if (age >= 12 && age < 26) {
            percentage = discounts.get("student");
        }

        return percentage != null ? BigDecimal.valueOf(percentage) : BigDecimal.ZERO;
    }

    /** Recargo si el asiento es preferencial */
    private BigDecimal calculateSeatSurcharge(Seat seat, BigDecimal basePrice) {
        if (seat.getType() == SeatType.PREFERENTIAL) {
            return basePrice.multiply(new BigDecimal("0.15"));
        }
        return BigDecimal.ZERO;
    }

    /** Calcula el recargo dinámico en función de la ocupación */
    private BigDecimal calculateDynamicSurcharge(Long tripId, Long busId, BigDecimal basePrice) {

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado con ID: " + busId));

        long sold = ticketRepository.countSoldByTrip(tripId);
        double occupancy = (double) sold / bus.getCapacity();

        if (occupancy >= 0.85) {
            return basePrice.multiply(new BigDecimal("0.20"));
        } else if (occupancy >= 0.70) {
            return basePrice.multiply(new BigDecimal("0.10"));
        }

        return BigDecimal.ZERO;
    }
}

