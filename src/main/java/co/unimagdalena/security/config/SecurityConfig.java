package co.unimagdalena.security.config;

import co.unimagdalena.security.error.Http401EntryPoint;
import co.unimagdalena.security.error.Http403AccessDenied;
import co.unimagdalena.security.jwt.JwtAuthenticationFilter;
import co.unimagdalena.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final Http401EntryPoint http401EntryPoint;
    private final Http403AccessDenied http403AccessDenied;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ========== ENDPOINTS PÚBLICOS ==========
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh-token",
                                "/error",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Catálogo público - ver rutas y viajes
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/routes/**",
                                "/api/v1/trips/**"
                        ).permitAll()

                        // ========== USUARIOS Y AUTENTICACIÓN ==========
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profile").authenticated()
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        // ========== PASAJEROS ==========
                        .requestMatchers(HttpMethod.POST, "/api/v1/passengers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/passengers/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/passengers/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/passengers/**").hasAnyRole("CLERK", "ADMIN")

                        // ========== COMPRAS Y TICKETS ==========
                        .requestMatchers(HttpMethod.POST, "/api/v1/purchases/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/purchases/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets/validate-qr").hasAnyRole("DRIVER", "DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers("/api/v1/tickets/**").hasAnyRole("PASSENGER", "DRIVER", "DISPATCHER", "CLERK", "ADMIN")

                        // ========== VIAJES Y RUTAS ==========
                        .requestMatchers(HttpMethod.POST, "/api/v1/trips/**").hasAnyRole("DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trips/**").hasAnyRole("DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/trips/**").hasAnyRole("DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/trips/**").hasAnyRole("CLERK", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/routes/**").hasAnyRole("CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/routes/**").hasAnyRole("CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/routes/**").hasAnyRole("CLERK", "ADMIN")

                        // ========== BUSES Y ASIENTOS ==========
                        .requestMatchers(HttpMethod.GET, "/api/v1/buses/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/buses/**").hasAnyRole("CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/buses/**").hasAnyRole("CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/buses/**").hasAnyRole("CLERK", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/seats/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/seats/**").hasAnyRole("CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/seats/**").hasAnyRole("CLERK", "ADMIN")

                        // ========== PARADAS ==========
                        .requestMatchers("/api/v1/stops/**").hasAnyRole("CLERK", "ADMIN")

                        // ========== RESERVAS DE ASIENTOS ==========
                        .requestMatchers("/api/v1/seat-holds/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")

                        // ========== EQUIPAJE ==========
                        .requestMatchers("/api/v1/baggage/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")

                        // ========== ENCOMIENDAS ==========
                        .requestMatchers(HttpMethod.GET, "/api/v1/parcels/**").hasAnyRole("PASSENGER", "DRIVER", "DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/parcels/**").hasAnyRole("PASSENGER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/parcels/**").hasAnyRole("DRIVER", "DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/parcels/**").hasAnyRole("DRIVER", "DISPATCHER", "CLERK", "ADMIN")

                        // ========== ASIGNACIONES ==========
                        .requestMatchers(HttpMethod.GET, "/api/v1/assignments/driver/**").hasAnyRole("DRIVER", "DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers("/api/v1/assignments/**").hasAnyRole("DISPATCHER", "CLERK", "ADMIN")

                        // ========== REGLAS DE TARIFA ==========
                        .requestMatchers(HttpMethod.GET, "/api/v1/fare-rules/**").authenticated()
                        .requestMatchers("/api/v1/fare-rules/**").hasAnyRole("CLERK", "ADMIN")

                        // ========== INCIDENTES ==========
                        .requestMatchers(HttpMethod.POST, "/api/v1/incidents/**").hasAnyRole("DRIVER", "DISPATCHER", "CLERK", "ADMIN")
                        .requestMatchers("/api/v1/incidents/**").hasAnyRole("DISPATCHER", "CLERK", "ADMIN")

                        // ========== CONFIGURACIÓN ==========
                        .requestMatchers("/api/v1/config/**").hasRole("ADMIN")

                        // ========== PAGOS Y CAJA ==========
                        .requestMatchers("/api/v1/payments/confirm").hasAnyRole("PASSENGER", "CLERK")
                        .requestMatchers("/api/v1/cash/close").hasAnyRole("CLERK", "DRIVER")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(http401EntryPoint)
                        .accessDeniedHandler(http403AccessDenied)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}