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
                .authorizeHttpRequests(authz -> authz
                        // Actuator 엔드포인트 허용
                        .requestMatchers("/actuator/**").permitAll()
                        // Health 엔드포인트 허용
                        .requestMatchers("/health").permitAll()
                        // API
                        .requestMatchers("/api/**").permitAll()
                        // 기타 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // HTTP Basic 인증 사용
                .httpBasic(httpBasic -> httpBasic
                        .realmName("Flik Application")
                )
                // CSRF 비활성화 (REST API이므로)
                .csrf(csrf -> csrf.disable())
                // CORS 설정 (nginx에서 처리하므로 허용)
                .cors(cors -> cors.disable());

        return http.build();
    }
}