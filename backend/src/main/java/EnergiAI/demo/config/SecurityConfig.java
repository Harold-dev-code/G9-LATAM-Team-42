package EnergiAI.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para el entorno de desarrollo local.
 *
 * Deshabilita CSRF y deja todos los endpoints públicos (permitAll) para poder
 * probarlos desde Postman sin token ni contraseña. Esto también evita que
 * Spring Security genere la "generated security password" en la consola y
 * elimina los errores 401 Unauthorized.
 *
 * NOTA: Esta configuración es apta solo para desarrollo. Antes de producción
 * debe reemplazarse por reglas de autenticación/autorización reales.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF (necesario para permitir POST desde Postman sin token)
                .csrf(csrf -> csrf.disable())
                // Sin estado: no crea sesiones HTTP
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Todos los endpoints quedan públicos
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Deshabilita el formulario de login y el basic auth por defecto
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
