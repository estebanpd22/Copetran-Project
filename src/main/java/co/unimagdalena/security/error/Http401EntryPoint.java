package co.unimagdalena.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class Http401EntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Intento de acceso no autorizado a: {} {} - {}",
                request.getMethod(), request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "Autenticación requerida",
                "path", request.getRequestURI(),
                "timestamp", LocalDateTime.now(),
                "details", "Token JWT inválido o expirado"
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // Para soportar Java Time
            mapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception e) {
            log.error("Error escribiendo respuesta 401: {}", e.getMessage());
            // Respuesta de fallback
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
        }
    }
}