package co.unimagdalena.services;

import co.unimagdalena.api.dto.CashCloseDto.*;

import java.time.OffsetDateTime;
import java.util.List;

public interface CashCloseService {
    CashCloseResponse closeCash(CashCloseRequest request);
    CashCloseResponse getCashCloseById(Long id);
    List<CashCloseResponse> getCashClosesByUser(Long userId);
    List<CashCloseResponse> getCashClosesByTrip(Long tripId);
    List<CashCloseResponse> getCashClosesByDateRange(OffsetDateTime startDate, OffsetDateTime endDate);
}
