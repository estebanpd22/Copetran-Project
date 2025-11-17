package co.unimagdalena.services.mappers;

import co.unimagdalena.api.dto.FareRuleDto;
import co.unimagdalena.domine.entities.DynamicPricing;
import co.unimagdalena.domine.entities.FareRule;
import co.unimagdalena.domine.entities.Route;
import co.unimagdalena.domine.entities.Stop;
import co.unimagdalena.services.mapper.FareRuleMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FareRuleMapperTest {
    private final FareRuleMapper mapper = Mappers.getMapper(FareRuleMapper.class);

    @Test
    void toEntity_shouldMapToCreate() {
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("student", 0.15);
        discounts.put("senior", 0.20);

        FareRuleDto.FareRuleCreateRequest request = new FareRuleDto.FareRuleCreateRequest(
                new BigDecimal("50.00"),
                DynamicPricing.ON,
                discounts,
                1L,
                1L,
                2L
        );

        FareRule fareRule = mapper.toEntity(request);

        assertThat(fareRule.getBasePrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(fareRule.getDynamicPricing()).isEqualTo(DynamicPricing.ON);
        assertThat(fareRule.getDiscounts()).containsEntry("student", 0.15);
    }

    @Test
    void toResponse_shouldMapEntity() {
        Route route = Route.builder().id(1L).code("R001").name("Route 1").build();
        Stop fromStop = Stop.builder().id(1L).name("Stop A").order(1).build();
        Stop toStop = Stop.builder().id(2L).name("Stop B").order(2).build();

        Map<String, Double> discounts = new HashMap<>();
        discounts.put("student", 0.15);

        FareRule fareRule = FareRule.builder()
                .id(1L)
                .basePrice(new BigDecimal("50.00"))
                .dynamicPricing(DynamicPricing.OFF)
                .discounts(discounts)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .build();

        FareRuleDto.FareRuleResponse dto = mapper.toResponse(fareRule);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(dto.dynamicPricing()).isEqualTo(DynamicPricing.OFF);
        assertThat(dto.route()).isNotNull();
        assertThat(dto.fromStop()).isNotNull();
        assertThat(dto.toStop()).isNotNull();
    }

    @Test
    void updateEntity_shouldUpdateFields() {
        FareRule fareRule = FareRule.builder()
                .id(1L)
                .basePrice(new BigDecimal("50.00"))
                .dynamicPricing(DynamicPricing.OFF)
                .build();

        FareRuleDto.FareRuleUpdateRequest update = new FareRuleDto.FareRuleUpdateRequest(
                new BigDecimal("60.00"),
                DynamicPricing.ON,
                null,
                null,
                null,
                null
        );

        mapper.updateEntity(update, fareRule);

        assertThat(fareRule.getBasePrice()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(fareRule.getDynamicPricing()).isEqualTo(DynamicPricing.ON);
    }
}
