package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.FareRule;
import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.api.dto.RouteDto.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class FareRuleDto {

    public record FareRuleCreateRequest(
            @NotNull BigDecimal basePrice,
            @NotNull FareRule.dinamyPricing dinamyPricing,
            Map<String, Double> discounts,
            @NotNull Long routeId,
            @NotNull Long fromStopId,
            @NotNull Long toStopId
    ) implements Serializable {}

    public record FareRuleUpdateRequest(
            BigDecimal basePrice,
            FareRule.dinamyPricing dinamyPricing,
            Map<String, Double> discounts,
            Long routeId,
            Long fromStopId,
            Long toStopId
    ) implements Serializable {}

    public record FareRuleResponse(
            Long id,
            BigDecimal basePrice,
            FareRule.dinamyPricing dinamyPricing,
            Map<String, Double> discounts,
            RouteSummary route,
            StopSummary fromStop,
            StopSummary toStop
    ) implements Serializable {}
}
