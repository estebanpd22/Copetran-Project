package co.unimagdalena.api.dto;

import co.unimagdalena.domine.entities.FareRule;
import co.unimagdalena.domine.entities.DynamicPricing;
import co.unimagdalena.api.dto.StopDto.*;
import co.unimagdalena.api.dto.RouteDto.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class FareRuleDto {

    public record FareRuleCreateRequest(
            @NotNull BigDecimal basePrice,
            @NotNull DynamicPricing dynamicPricing,
            Map<String, Double> discounts,
            @NotNull Long routeId,
            @NotNull Long fromStopId,
            @NotNull Long toStopId
    ) implements Serializable {}

    public record FareRuleUpdateRequest(
            BigDecimal basePrice,
            DynamicPricing dynamicPricing,
            Map<String, Double> discounts,
            Long routeId,
            Long fromStopId,
            Long toStopId
    ) implements Serializable {}

    public record FareRuleResponse(
            Long id,
            BigDecimal basePrice,
            DynamicPricing dynamicPricing,
            Map<String, Double> discounts,
            RouteSummary route,
            StopSummary fromStop,
            StopSummary toStop
    ) implements Serializable {}
}
