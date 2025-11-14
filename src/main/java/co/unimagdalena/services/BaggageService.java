package co.unimagdalena.services;

import co.unimagdalena.api.dto.BaggageDto.*;
import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.domine.entities.Baggage;
import co.unimagdalena.domine.entities.Ticket;

import java.math.BigDecimal;
import java.util.List;

public interface BaggageService {

    Baggage createBaggage(BaggageCreateRequest request, Ticket ticket);
    BaggageResponse updateBaggage(Long id, BaggageUpdateRequest request);
    void deleteBaggage(Long id);

    BigDecimal calculateFee(Double weightKg);
    void assignTagCode(Long baggageId, String tagCode);

    BaggageResponse getBaggageById(Long id);
    BaggageResponse getBaggageByTagCode(String tagCode);
    List<BaggageResponse> getBaggageByTicketId(Long ticketId);
}