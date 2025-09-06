package yunrry.flik.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@TestConfiguration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        // Redis 대신 단순 메모리 캐시 사용
        return new ConcurrentMapCacheManager();
    }

    // RedisTemplate Mock은 유지 (다른 곳에서 사용할 수 있음)
    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(mockRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisConnectionFactory mockRedisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }
}