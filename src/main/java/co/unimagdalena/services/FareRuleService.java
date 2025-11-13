package co.unimagdalena.services;

import co.unimagdalena.api.dto.FareRuleDto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FareRuleService {

    FareRuleResponse createFareRule(FareRuleCreateRequest request);
    void updateFareRule(Long id, FareRuleUpdateRequest request);
    void deleteFareRule(Long id);

    FareRuleResponse getFareRule(Long id);
    List<FareRuleResponse> getAllFareRules();

    List<FareRuleResponse> getFareRulesByRouteId(Long routeId);
    BigDecimal getFinalTicketPrice(
            Long routeId,
            Long fromStopId,
            Long toStopId,
            Long passengerId,
            Long busId,
            String seatNumber,
            Long TripId
    );
}