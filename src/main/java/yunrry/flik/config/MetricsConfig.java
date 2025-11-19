package yunrry.flik.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                    .commonTags("application", "flik", "environment", "local")
                    .meterFilter(MeterFilter.deny(id -> {
                        // 불필요한 메트릭 제외
                        String name = id.getName();
                        return name.startsWith("jvm.gc.pause") ||
                                name.startsWith("jvm.buffer");
                    }))
                    .meterFilter(MeterFilter.accept()); // 모든 HTTP 메트릭 허용
        };
    }

    @Bean
    public Timer.Builder customTimerBuilder() {
        return Timer.builder("http.server.requests")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram();
    }
}