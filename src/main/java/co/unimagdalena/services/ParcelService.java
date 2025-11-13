package co.unimagdalena.services;

import co.unimagdalena.api.dto.ParcelDto.*;

import java.util.List;

public interface ParcelService {

    ParcelResponse createParcel(ParcelCreateRequest request);
    void updateParcel(Long parcelId, ParcelUpdateRequest request);
    void assignTrip(Long parcelId, Long tripId);

    void confirmDelivery(Long parcelId, String otp, String proofPhotoUrl);
    void markDeliveryFailed(Long parcelId, String failureNote);

    ParcelResponse getParcelByCode(String code);
    List<ParcelResponse> getParcelsByTrip(Long tripId);
    List<ParcelResponse> getParcelsBySender(String senderPhone);
    List<ParcelResponse> getParcelsByReceiver(String receiverPhone);
}
