package co.unimagdalena.services.mapper;

import co.unimagdalena.api.dto.CashCloseDto.*;
import co.unimagdalena.domine.entities.CashClose;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CashCloseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    CashClose toEntity(CashCloseRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(cashClose.getUser() != null ? cashClose.getUser().getFullName() : null)")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "tripRoute", expression = "java(cashClose.getTrip() != null && cashClose.getTrip().getRoute() != null ? cashClose.getTrip().getRoute().getName() : null)")
    @Mapping(target = "discrepancy", expression = "java(calculateDiscrepancy(cashClose))")
    @Mapping(target = "status", source = "status")
    CashCloseResponse toResponse(CashClose cashClose);

    default BigDecimal calculateDiscrepancy(CashClose cashClose) {
        if (cashClose.getExpectedAmount() == null || cashClose.getActualAmount() == null) {
            return BigDecimal.ZERO;
        }
        return cashClose.getActualAmount().subtract(cashClose.getExpectedAmount());
    }
}
