package co.unimagdalena.notification;

public enum NotificationTemplate {

    PURCHASE_CONFIRMED(
            "✅ *Compra Confirmada - BusTransport*\n\n" +
                    "🎫 Detalles de tu viaje:\n" +
                    "📍 Origen: %s\n" +
                    "📍 Destino: %s\n" +
                    "📅 Fecha: %s\n" +
                    "🕐 Hora salida: %s\n" +
                    "💺 Asiento(s): %s\n" +
                    "💰 Total pagado: $%s\n\n" +
                    "🔖 Código de reserva: %s\n\n" +
                    "¡Buen viaje! 🚌"
    ),

    PLATFORM_CHANGE(
            "⚠️ *Cambio de Andén - BusTransport*\n\n" +
                    "Tu viaje ha sido actualizado:\n" +
                    "📍 Ruta: %s → %s\n" +
                    "📅 Fecha: %s\n" +
                    "🚏 Nuevo andén: %s\n" +
                    "🕐 Hora salida: %s\n\n" +
                    "Por favor dirígete al nuevo andén."
    ),

    ARRIVAL_SOON(
            "🚌 *Bus Próximo a Llegar - BusTransport*\n\n" +
                    "Tu bus está por llegar:\n" +
                    "📍 Destino: %s\n" +
                    "🕐 Llegada estimada: %s\n" +
                    "🚏 Andén: %s\n" +
                    "💺 Asiento: %s\n\n" +
                    "Ten lista tu documentación."
    );

    private final String template;

    NotificationTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }
}
