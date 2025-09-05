package yunrry.flik.adapters.out.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import yunrry.flik.core.domain.model.RefreshToken;
import yunrry.flik.ports.out.repository.RefreshTokenRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "refresh_token:";

    @Override
    public void save(RefreshToken refreshToken) {
        String key = KEY_PREFIX + refreshToken.getUserId();
        try {
            RefreshTokenData data = new RefreshTokenData(
                    refreshToken.getToken(),
                    refreshToken.getUserId(),
                    refreshToken.getExpiryTime()
            );
            String value = objectMapper.writeValueAsString(data);

            Duration ttl = Duration.between(LocalDateTime.now(), refreshToken.getExpiryTime());
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize refresh token", e);
        }
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        try {
            RefreshTokenData data = objectMapper.readValue(value, RefreshTokenData.class);
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(data.token())
                    .userId(data.userId())
                    .expiryTime(data.expiryTime())
                    .build();
            return Optional.of(refreshToken);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByToken(String token) {
        // Redis에서 토큰 값으로 검색하는 것은 비효율적이므로
        // 실제로는 사용하지 않는 메서드지만 인터페이스 구현을 위해 빈 구현
    }

    private record RefreshTokenData(
            String token,
            Long userId,
            LocalDateTime expiryTime
    ) {}
}