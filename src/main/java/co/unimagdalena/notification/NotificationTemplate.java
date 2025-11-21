package co.unimagdalena.notification;

import lombok.Getter;

@Getter
public enum NotificationTemplate {

    PURCHASE_CONFIRMED(
            "🚌 *Copetran - Compra Confirmada*\n\n" +
                    "✅ Su reserva ha sido confirmada exitosamente\n\n" +
                    "📋 *Detalles del Viaje:*\n" +
                    "• Ruta: %s → %s\n" +
                    "• Fecha: %s\n" +
                    "• Hora salida: %s\n" +
                    "• Asiento: %s\n" +
                    "• Valor: $%,.2f\n\n" +
                    "🎫 *Código de Reserva:* %s\n" +
                    "📱 *Presente este código al abordar*\n\n" +
                    "¡Gracias por elegirnos! ✨"
    ),

    PLATFORM_CHANGE(
            "🔄 *Copetran - Cambio de Andén*\n\n" +
                    "📢 *Información importante sobre su viaje:*\n\n" +
                    "• Ruta: %s → %s\n" +
                    "• Fecha: %s\n" +
                    "• Hora: %s\n" +
                    "• 🔁 *Nuevo andén:* %s\n\n" +
                    "📍 Por favor diríjase al andén asignado\n" +
                    "⏰ Recomendamos llegar 15 minutos antes"
    ),

    ARRIVAL_SOON(
            "📍 *Copetran - Próxima Llegada*\n\n" +
                    "Su destino está cerca\n\n" +
                    "🎯 *Información:*\n" +
                    "• Destino: %s\n" +
                    "• Llegada estimada: %s\n" +
                    "• Andén: %s\n" +
                    "• Asiento: %s\n\n" +
                    "📝 Tenga a mano su documento de identidad\n" +
                    "🧳 Verifique su equipaje personal"
    ),

    TICKET_CANCELLED(
            "❌ *Copetran - Ticket Cancelado*\n\n" +
                    "📋 *Detalles de la cancelación:*\n" +
                    "• Ruta: %s → %s\n" +
                    "• Fecha: %s\n" +
                    "• Asiento: %s\n" +
                    "• Motivo: %s\n\n" +
                    "💰 *Reembolso:* Será procesado según nuestra política\n" +
                    "📞 Para más información contacte a servicio al cliente"
    );

    private final String template;

    NotificationTemplate(String template) {
        this.template = template;
    }

    public String format(Object... args) {
            return String.format(template, args);
    }
}