package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.domine.entities.Baggage;
import co.unimagdalena.domine.entities.Ticket;
import co.unimagdalena.domine.repositories.BaggageRepository;
import co.unimagdalena.domine.repositories.TicketRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.BaggageService;
import co.unimagdalena.services.mapper.BaggageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BaggageServiceImpl implements BaggageService {

    private final BaggageRepository baggageRepository;
    private final TicketRepository ticketRepository;
    private final BaggageMapper mapper;

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------
    @Override
    public Baggage createBaggage(BaggageCreateRequest request, Ticket ticket) {

        if (ticket == null) {
            throw new IllegalArgumentException("Ticket is required to register baggage");
        }

        // Validación básica
        if (request.weightKg() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }

        Baggage baggage = mapper.toEntity(request);

        baggage.setTicket(ticket);
        baggage.setFee(calculateFee(Double.valueOf(request.weightKg())));

        baggageRepository.save(baggage);

        return baggage;
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
    @Override
    public BaggageResponse updateBaggage(Long id, BaggageUpdateRequest request) {

        Baggage baggage = baggageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Baggage not found"));

        mapper.updateEntity(request, baggage);

        // Si cambió el peso se recalcula la tarifa
        if (request.weightKg() != null) {
            baggage.setFee(calculateFee(Double.valueOf(request.weightKg())));
        }

        baggageRepository.save(baggage);

        return mapper.toResponse(baggage);
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------
    @Override
    public void deleteBaggage(Long id) {
        if (!baggageRepository.existsById(id)) {
            throw new NotFoundException("Baggage not found");
        }
        baggageRepository.deleteById(id);
    }

    // -------------------------------------------------------
    // CALCULATE FEE
    // Regla de ejemplo:
    // si el equipaje pesa más de 20 kg → se cobra por exceso
    // (ajústalo si tu profe dio una regla diferente)
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(Double weightKg) {

        if (weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }

        // Regla de negocio del PDF (puedes adaptarla):
        double baseAllowed = 20.0;
        double extraPerKg = 1.5; // ejemplo

        if (weightKg <= baseAllowed) {
            return BigDecimal.ZERO;
        }

        double extra = (weightKg - baseAllowed) * extraPerKg;

        return BigDecimal.valueOf(extra).setScale(2, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------
    // ASSIGN TAG CODE
    // -------------------------------------------------------
    @Override
    public void assignTagCode(Long baggageId, String tagCode) {

        Baggage baggage = baggageRepository.findById(baggageId)
                .orElseThrow(() -> new NotFoundException("Baggage not found"));

        if (tagCode == null || tagCode.isBlank()) {
            throw new IllegalArgumentException("Tag code cannot be empty");
        }

        // Evitar que dos equipajes tengan el mismo tag
        if (baggageRepository.existsByTagCode(tagCode)) {
            throw new IllegalStateException("Tag code already in use");
        }

        baggage.setTagCode(tagCode);
        baggageRepository.save(baggage);
    }

    // -------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public BaggageResponse getBaggageById(Long id) {

        Baggage baggage = baggageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Baggage not found"));

        return mapper.toResponse(baggage);
    }

    // -------------------------------------------------------
    // GET BY TAG CODE
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public BaggageResponse getBaggageByTagCode(String tagCode) {

        if (tagCode == null || tagCode.isBlank()) {
            throw new IllegalArgumentException("Tag code is required");
        }

        Baggage baggage = baggageRepository.findByTagCode(tagCode)
                .orElseThrow(() -> new NotFoundException("Baggage not found"));

        return mapper.toResponse(baggage);
    }

    // -------------------------------------------------------
    // GET BY TICKET
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<BaggageResponse> getBaggageByTicketId(Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new NotFoundException("Ticket not found");
        }

        return baggageRepository.findByTicketId(ticketId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

