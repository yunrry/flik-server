package yunrry.flik.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import yunrry.flik.core.service.auth.JwtTokenProvider;
import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        if (token != null) {
            log.debug("[JWT Filter] Token found for {} {}", method, requestUri);

            // 관리자 토큰 우선 검증
            if (jwtTokenProvider.validateAdminToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                log.debug("[JWT Filter] Admin token validated. userId={}", userId);

                Collection<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 일반 토큰 검증
            else if (jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                log.debug("[JWT Filter] User token validated. userId={}", userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.debug("[JWT Filter] Invalid token for {} {}", method, requestUri);
            }
        } else {
            log.debug("[JWT Filter] No token found for {} {}", method, requestUri);
        }

        // 현재 SecurityContextHolder 상태 로그
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.debug("[JWT Filter] SecurityContext authentication principal: {}", principal);
        } else {
            log.debug("[JWT Filter] SecurityContext has no authentication set");
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
