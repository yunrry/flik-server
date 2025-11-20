package yunrry.flik.core.service;

import io.micrometer.core.instrument.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Timer> embeddingTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public Timer.Sample startEmbeddingTimer() {
        log.debug("Starting embedding timer");
        return Timer.start(meterRegistry);
    }

    public Timer.Sample startOpenAITimer() {
        log.debug("Starting OpenAI API timer");
        return Timer.start(meterRegistry);
    }

    public void recordEmbeddingTime(Timer.Sample sample, String operation) {
        Timer timer = embeddingTimers.computeIfAbsent(operation, op ->
                Timer.builder("embedding_generation_time")
                        .description("Time taken to generate embeddings")
                        .tag("operation", op)
                        .register(meterRegistry)
        );
        sample.stop(timer);
        log.debug("Recorded embedding time for operation: {}", operation);
    }


    public void recordOpenAIAPITime(Timer.Sample sample, String apiType) {
        Timer timer = embeddingTimers.computeIfAbsent("openai_api_call_" + apiType, op ->
                Timer.builder("openai_api_call_time")
                        .description("Time taken for OpenAI API calls")
                        .tag("api_type", apiType)
                        .register(meterRegistry)
        );
        sample.stop(timer);
        log.debug("Recorded OpenAI API call time for type: {}", apiType);
    }
    // 카운터들
    public void incrementUserRegistration() {
        Counter.builder("user_registration_count_total")
                .description("Number of user registrations")
                .register(meterRegistry)
                .increment();
    }

    public void incrementSwipe() {
        counters.computeIfAbsent("user_swipe_count_total", name ->
                Counter.builder(name)
                        .description("Number of swipes")
                        .register(meterRegistry)
        ).increment();
    }

    public void incrementSpotSave() {
        Counter.builder("user_spot_save_count_total")
                .description("Number of spots saved by users")
                .register(meterRegistry)
                .increment();
    }

    public void incrementCourseGeneration(String region) {
        Counter.builder("generation_count_total")
                .description("Number of course generation")
                .tag("region", region)
                .register(meterRegistry)
                .increment();
    }

    public void incrementEmbedding(String category) {
        Counter.builder("embedding_generation_count_total")
                .description("Number of embeddings generated")
                .tag("category", category)
                .register(meterRegistry)
                .increment();
    }

    public void incrementCacheHit(String cacheType) {
        Counter.builder("cache_hit_count_total")
                .description("Number of cache hits")
                .tag("cache_type", cacheType)
                .register(meterRegistry)
                .increment();
    }

    public void incrementCacheMiss(String cacheType) {
        Counter.builder("cache_miss_count_total")
                .description("Number of cache misses")
                .tag("cache_type", cacheType)
                .register(meterRegistry)
                .increment();
    }



    // 여행 코스 수정 추적
    public void recordSpotDeletionFromCourse(int count) {
        Counter.builder("travel_course_spot_deletion_count_total")
                .description("Number of spots deleted from travel course")
                .register(meterRegistry)
                .increment(count);
    }

    public void recordSpotAdditionToCourse(int count) {
        Counter.builder("travel_course_spot_addition_count_total")
                .description("Number of spots added to travel course")
                .register(meterRegistry)
                .increment(count);
    }

    public void recordSpotFeatureVector(String operation, String category, double[] vector) {
        meterRegistry.gauge("spot_feature_vector_magnitude",
                Tags.of("operation", operation, "category", category),
                calculateVectorMagnitude(vector));
    }

    private double calculateVectorMagnitude(double[] vector) {
        double sum = 0.0;
        for (double v : vector) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

}