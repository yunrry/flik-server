package yunrry.flik.core.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yunrry.flik.core.domain.exception.InvalidTokenException;
import yunrry.flik.core.domain.model.RefreshToken;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiry:3600}")  // 1시간
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry:604800}")  // 7일
    private long refreshTokenExpiry;

    // core/service/JwtTokenProvider.java - 메서드 추가
    public String createAdminToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofDays(30).toMillis()); // 한 달

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", "ADMIN")
                .claim("type", "admin")
                .setIssuedAt(now)
                .setExpiration(expiry)  // 한 달 만료 시간 추가
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAdminToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();

            return "admin".equals(claims.get("type")) && !isTokenExpired(claims);
        } catch (ExpiredJwtException e) {
            return false; // 만료된 토큰
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }


    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiry * 1000);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public RefreshToken createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiry * 1000);

        String token = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiryTime(LocalDateTime.ofInstant(expiry.toInstant(), ZoneId.systemDefault()))
                .build();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()  // parserBuilder() → parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}