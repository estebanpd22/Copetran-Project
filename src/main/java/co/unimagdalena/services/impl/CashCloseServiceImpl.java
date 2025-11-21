package co.unimagdalena.services.impl;

import co.unimagdalena.api.dto.CashCloseDto.*;
import co.unimagdalena.domine.entities.*;
import co.unimagdalena.domine.repositories.CashCloseRepository;
import co.unimagdalena.domine.repositories.TripRepository;
import co.unimagdalena.domine.repositories.UserRepository;
import co.unimagdalena.exception.NotFoundException;
import co.unimagdalena.services.CashCloseService;
import co.unimagdalena.services.mapper.CashCloseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CashCloseServiceImpl implements CashCloseService {

    private final CashCloseRepository cashCloseRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final CashCloseMapper cashCloseMapper;

    @Override
    public CashCloseResponse closeCash(CashCloseRequest request) {
        log.info("Processing cash close for user: {}", request.userId());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.userId()));

        // Validate user role
        if (user.getRole() != UserRole.CLERK && 
            user.getRole() != UserRole.DRIVER && 
            user.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("User does not have permission to close cash");
        }

        CashClose cashClose = cashCloseMapper.toEntity(request);
        cashClose.setUser(user);
        cashClose.setClosedAt(OffsetDateTime.now());

        // Set trip if provided
        if (request.tripId() != null) {
            Trip trip = tripRepository.findById(request.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip not found with id: " + request.tripId()));
            cashClose.setTrip(trip);
        }

        // Calculate discrepancy and set status
        BigDecimal discrepancy = request.actualAmount().subtract(request.expectedAmount());
        BigDecimal tolerance = new BigDecimal("10.00"); // $10 tolerance

        if (discrepancy.abs().compareTo(tolerance) <= 0) {
            cashClose.setStatus(CashCloseStatus.RECONCILED);
        } else {
            cashClose.setStatus(CashCloseStatus.DISCREPANCY);
        }

        CashClose savedCashClose = cashCloseRepository.save(cashClose);
        log.info("Cash close completed with id: {} - Status: {}", savedCashClose.getId(), savedCashClose.getStatus());

        return cashCloseMapper.toResponse(savedCashClose);
    }

    @Override
    @Transactional(readOnly = true)
    public CashCloseResponse getCashCloseById(Long id) {
        log.info("Fetching cash close: {}", id);

        CashClose cashClose = cashCloseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cash close not found with id: " + id));

        return cashCloseMapper.toResponse(cashClose);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashCloseResponse> getCashClosesByUser(Long userId) {
        log.info("Fetching cash closes for user: {}", userId);

        return cashCloseRepository.findByUserId(userId).stream()
                .map(cashCloseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashCloseResponse> getCashClosesByTrip(Long tripId) {
        log.info("Fetching cash closes for trip: {}", tripId);

        return cashCloseRepository.findByTripId(tripId).stream()
                .map(cashCloseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashCloseResponse> getCashClosesByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Fetching cash closes between {} and {}", startDate, endDate);

        return cashCloseRepository.findByClosedAtBetween(startDate, endDate).stream()
                .map(cashCloseMapper::toResponse)
                .collect(Collectors.toList());
    }
}
