package co.unimagdalena.services;

import co.unimagdalena.api.dto.PassengerDto.PassengerCreateRequest;
import co.unimagdalena.api.dto.PassengerDto.PassengerResponse;
import co.unimagdalena.api.dto.PassengerDto.PassengerUpdateRequest;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PassengerService {

    PassengerResponse createPassenger(PassengerCreateRequest request);
    void updatePassenger(Long id,PassengerUpdateRequest request);
    void deletePassenger(Long id);
    List<PassengerResponse> getPassengerByUser(Long userId);
    PassengerResponse finByDocumentNumber(@Param("documentNumber") String documentNumber);
    PassengerResponse getPassengerById(@Param("id") Long id);
}
