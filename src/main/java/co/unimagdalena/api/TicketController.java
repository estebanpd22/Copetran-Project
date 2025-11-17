package co.unimagdalena.api;

import co.unimagdalena.api.dto.TicketDto.*;
import co.unimagdalena.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/tickets")
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<TicketResponse>> getByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(ticketService.getTicketsByTrip(tripId));
    }

    @GetMapping("/purchase/{purchaseId}")
    public ResponseEntity<List<TicketResponse>> getByPurchase(@PathVariable Long purchaseId) {
        return ResponseEntity.ok(ticketService.getTicketsByPurchase(purchaseId));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<TicketResponse>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(ticketService.getTicketsByPassenger(passengerId));
    }

    @PostMapping("/{id}/generate-qr")
    public ResponseEntity<Void> generateQr(@PathVariable Long id) {
        ticketService.generateQrForTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate-qr")
    public ResponseEntity<Void> validateQr(@RequestParam String qrCode) {
        ticketService.validateQrForTicket(qrCode);
        return ResponseEntity.ok().build();
    }
}
