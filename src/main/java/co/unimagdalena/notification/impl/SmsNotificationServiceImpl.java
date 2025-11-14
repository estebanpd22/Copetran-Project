package co.unimagdalena.notification.impl;

import co.unimagdalena.notification.NotificationRequest;
import co.unimagdalena.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationServiceImpl extends AbstractNotificationService {

    @Override
    protected void sendMessage(NotificationRequest request) {

        log.info("╔═══════════════════════════════════════════════════════╗");
        log.info("║              📲 SMS NOTIFICATION (MOCK)               ║");
        log.info("╠═══════════════════════════════════════════════════════╣");
        log.info("║ Para: {}", String.format("%-44s", request.recipient()) + "║");
        log.info("╠═══════════════════════════════════════════════════════╣");

        // SMS tiene límite de caracteres, mostramos advertencia
        int messageLength = request.message().length();
        if (messageLength > 160) {
            int parts = (int) Math.ceil(messageLength / 160.0);
            log.info("║ ⚠️  Mensaje largo: {} caracteres ({} SMS)            ║",
                    messageLength, parts);
            log.info("╠═══════════════════════════════════════════════════════╣");
        }

        // Dividir mensaje en líneas
        String[] lines = request.message().split("\n");
        for (String line : lines) {
            // Truncar líneas muy largas para el log
            String displayLine = line.length() > 52 ? line.substring(0, 49) + "..." : line;
            log.info("║ {}", String.format("%-52s", displayLine) + "║");
        }

        log.info("╚═══════════════════════════════════════════════════════╝");

        simulateNetworkDelay();

    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(300); // 300ms de delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
