package yunrry.flik.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health",
                                "/actuator",
                                "/actuator/**",
                                "/api/actuator/health",
                                "/swagger-ui/**",
                                "/api/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/api/v1/**",
                                "/v1/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}