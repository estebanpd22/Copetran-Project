package co.unimagdalena.security.jwt;

import co.unimagdalena.security.user.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    @Value("${security.jwt.refresh-expiration-seconds:2592000}") // 30 días por defecto
    private long refreshExpirationSeconds;

    private SecretKey getSigningKey() {
        try {
            // Asegurar que el secret tenga al menos 32 caracteres para HS256
            byte[] keyBytes;
            if (secret.length() < 32) {
                log.warn("JWT secret es muy corto ({} caracteres). Se recomienda usar al menos 32 caracteres.", secret.length());
                // Extender el secret a 32 caracteres
                StringBuilder extendedSecret = new StringBuilder(secret);
                while (extendedSecret.length() < 32) {
                    extendedSecret.append("0");
                }
                keyBytes = extendedSecret.substring(0, 32).getBytes();
            } else {
                keyBytes = secret.getBytes();
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error al generar la clave de firma JWT", e);
            throw new RuntimeException("Error en configuración JWT", e);
        }
    }

    // ========== EXTRACCIÓN DE CLAIMS ==========

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractFullName(String token) {
        return extractClaim(token, claims -> claims.get("fullName", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            log.error("Error extrayendo claim del token JWT", e);
            throw new IllegalArgumentException("Token inválido o malformado");
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parseando token JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Token JWT inválido");
        }
    }

    // ========== GENERACIÓN DE TOKENS ==========

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expirationSeconds * 1000);
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("fullName", userDetails.getFullName());
        claims.put("role", userDetails.getRole());
        return createToken(claims, userDetails.getUsername(), expirationSeconds * 1000);
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("tokenType", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpirationSeconds * 1000);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Error generando token JWT", e);
            throw new RuntimeException("Error al generar token", e);
        }
    }

    // ========== VALIDACIÓN DE TOKENS ==========

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isRefreshTokenValid(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }
            // Verificar que sea un refresh token
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.warn("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean isAccessTokenValid(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }
            // Verificar que NO sea un refresh token
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return !"refresh".equals(tokenType);
        } catch (Exception e) {
            log.warn("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ========== UTILIDADES ==========

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error verificando expiración del token: {}", e.getMessage());
            return true;
        }
    }

    public Long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            return expiration.getTime() - now.getTime();
        } catch (Exception e) {
            log.warn("Error obteniendo tiempo restante del token: {}", e.getMessage());
            return 0L;
        }
    }

    public Map<String, Object> getAllClaims(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Map<String, Object> result = new HashMap<>();
            claims.forEach(result::put);
            return result;
        } catch (Exception e) {
            log.error("Error obteniendo todos los claims del token", e);
            throw new IllegalArgumentException("No se pudieron obtener los claims del token");
        }
    }

    // ========== VERIFICACIONES DE SEGURIDAD ==========

    public Boolean canTokenBeRefreshed(String token) {
        try {
            // Un token puede ser refrescado si no está expirado o si expiró hace menos de 5 minutos
            Date expiration = extractExpiration(token);
            Date now = new Date();
            Date fiveMinutesAgo = new Date(now.getTime() - 5 * 60 * 1000);
            return expiration.after(fiveMinutesAgo);
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            if (!isRefreshTokenValid(refreshToken)) {
                throw new IllegalArgumentException("Refresh token inválido");
            }

            Long userId = extractUserId(refreshToken);
            String username = extractUsername(refreshToken);

            // En una implementación real, cargarías el usuario de la base de datos
            // Por simplicidad, recreamos los claims básicos
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("fullName", extractFullName(refreshToken));
            claims.put("role", extractUserRole(refreshToken));

            return createToken(claims, username, expirationSeconds * 1000);
        } catch (Exception e) {
            log.error("Error refrescando token: {}", e.getMessage());
            throw new RuntimeException("Error al refrescar token", e);
        }
    }
}