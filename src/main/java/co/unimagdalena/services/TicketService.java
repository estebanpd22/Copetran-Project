package co.unimagdalena.services;

import co.unimagdalena.api.dto.PurchaseDto.*;
import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.domine.entities.Purchase;
import co.unimagdalena.domine.entities.Ticket;

import java.util.List;

public interface TicketService {

    Ticket createTicket(PurchaseCreateRequest.TicketRequest req, Purchase purchase);
    TicketResponse getTicket(Long id);
    void deleteTicket(Long id);

    void generateQrForTicket(Long ticketId);
    void validateQrForTicket(String qrCode);
    int expireUnusedTickets();
    void releaseSeatsByPurchase(Long purchaseId);

    List<TicketResponse> getTicketsByTrip(Long tripId);
    List<TicketResponse> getTicketsByPurchase(Long purchaseId);
    List<TicketResponse> getTicketsByPassenger(Long passengerId);
}
