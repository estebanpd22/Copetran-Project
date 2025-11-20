package co.unimagdalena.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class Http403AccessDenied implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String user = request.getUserPrincipal() != null ?
                request.getUserPrincipal().getName() : "ANONYMOUS";

        log.warn("Acceso denegado para usuario '{}' a: {} {} - {}",
                user, request.getMethod(), request.getRequestURI(),
                accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = Map.of(
                "status", 403,
                "error", "Forbidden",
                "message", "Acceso denegado",
                "path", request.getRequestURI(),
                "timestamp", LocalDateTime.now(),
                "details", "No tiene permisos para acceder a este recurso",
                "requiredRole", extractRequiredRole(accessDeniedException)
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            log.error("Error escribiendo respuesta 403: {}", e.getMessage());
            response.getWriter().write("{\"error\":\"Forbidden\"}");
        }
    }

    private String extractRequiredRole(AccessDeniedException exception) {
        String message = exception.getMessage();
        if (message != null && message.contains("hasRole")) {
            // Extraer el rol requerido del mensaje de Spring Security
            return message.replaceAll(".*hasRole\\('([^']+)'\\).*", "$1");
        }
        return "ROLE_REQUIRED";
    }
}
