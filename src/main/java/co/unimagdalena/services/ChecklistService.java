package co.unimagdalena.services;

import co.unimagdalena.api.dto.ChecklistDto.*;

public interface ChecklistService {
    ChecklistResponse createChecklist(ChecklistCreateRequest request);
    ChecklistResponse updateChecklist(Long checklistId, ChecklistUpdateRequest request);
    ChecklistResponse getChecklistById(Long checklistId);
    ChecklistResponse getChecklistByTripId(Long tripId);
    void deleteChecklist(Long checklistId);
    ChecklistResponse completeChecklist(Long checklistId, Long userId);
    boolean isChecklistComplete(Long tripId);
}
