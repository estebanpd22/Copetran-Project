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
                    "• Valor: $%,d\n\n" +
                    "🎫 *Código de Reserva:* %s\n" +
                    "📱 *Presente este código al abordar*\n\n" +
                    "¡Gracias por elegirnos! ✨"
    ),

    PLATFORM_CHANGE(
            "🔄 *Copetran - Cambio de Andén*\n\n" +
                    "📢 *Información importante sobre su viaje:*\n\n" +
                    "• Ruta: %s\n" +
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
    );

    private final String template;

    NotificationTemplate(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }
}
