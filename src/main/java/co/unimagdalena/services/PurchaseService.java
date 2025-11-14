package co.unimagdalena.services;

import co.unimagdalena.api.dto.PurchaseDto.*;

import java.time.OffsetDateTime;
import java.util.List;

public interface PurchaseService {

    PurchaseResponse createPurchase(PurchaseCreateRequest request);
    PurchaseResponse getPurchase(Long purchaseId);

    void cancelPurchase(Long purchaseId);
    void confirmPurchase(Long purchaseId, String paymentReference);

    List<PurchaseResponse> getPurchasesByUserId(Long userId);
    List<PurchaseResponse> getPurchasesByDateRange(OffsetDateTime start, OffsetDateTime end);
}