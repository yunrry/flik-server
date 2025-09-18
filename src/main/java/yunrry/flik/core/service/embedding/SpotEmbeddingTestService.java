package yunrry.flik.core.service.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.ports.out.repository.SpotEmbeddingRepository;
import yunrry.flik.ports.out.repository.UserSavedSpotRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotEmbeddingTestService {

    private final SpotEmbeddingRepository spotEmbeddingRepository;
    private final Random random = new Random();
    private final UserSavedSpotRepository userSavedSpotRepository;

    @Transactional
    public SpotEmbedding createTestEmbedding(Long spotId) {
        SpotEmbedding embedding = SpotEmbedding.builder()
                .spotId(spotId)
                .locationEmbedding(generateRandomLocationEmbedding())
                .tagEmbedding(generateRandomTagEmbedding())
                .build();

        SpotEmbedding saved = spotEmbeddingRepository.save(embedding);
        log.info("Created test embedding for spot: {}", spotId);
        return saved;
    }

    @Transactional
    public List<SpotEmbedding> createTestEmbeddings(List<Long> spotIds) {
        return spotIds.stream()
                .map(this::createTestEmbedding)
                .toList();
    }

    @Transactional
    public SpotEmbedding updateTestEmbedding(Long spotId) {
        Optional<SpotEmbedding> existing = spotEmbeddingRepository.findBySpotId(spotId);

        if (existing.isPresent()) {
            SpotEmbedding current = existing.get();
            SpotEmbedding updated = SpotEmbedding.builder()
                    .id(current.getId())
                    .spotId(current.getSpotId())
                    .locationEmbedding(generateRandomLocationEmbedding())
                    .tagEmbedding(generateRandomTagEmbedding())
                    .createdAt(current.getCreatedAt())
                    .updatedAt(current.getUpdatedAt())
                    .build();

            SpotEmbedding saved = spotEmbeddingRepository.save(updated);
            log.info("Updated test embedding for spot: {}", spotId);
            return saved;
        } else {
            return createTestEmbedding(spotId);
        }
    }

    public Optional<SpotEmbedding> getEmbedding(Long spotId) {
        return spotEmbeddingRepository.findBySpotId(spotId);
    }

    public List<SpotEmbedding> getAllEmbeddings() {
        return spotEmbeddingRepository.findAll();
    }

    public List<SpotEmbedding> getIncompleteEmbeddings() {
        return spotEmbeddingRepository.findIncompleteEmbeddings();
    }

    public List<SpotSimilarity> findSimilarSpots(Long userId, int limit) {
        String category = "cafe"; // 예시 카테고리
        List<Long> spotIds = userSavedSpotRepository.findSpotIdsByUserId(userId);
        return spotEmbeddingRepository.findSimilarSpotsByUserPreference(
                userId, category, spotIds, limit);
    }

    @Transactional
    public void deleteEmbedding(Long spotId) {
        spotEmbeddingRepository.deleteBySpotId(spotId);
        log.info("Deleted test embedding for spot: {}", spotId);
    }

    @Transactional
    public void deleteAllTestEmbeddings() {
        List<SpotEmbedding> all = spotEmbeddingRepository.findAll();
        all.forEach(embedding -> spotEmbeddingRepository.deleteBySpotId(embedding.getSpotId()));
        log.info("Deleted {} test embeddings", all.size());
    }

    // 테스트용 임베딩 생성 메서드들
    private List<Double> generateRandomLocationEmbedding() {
        // 2차원 위치 임베딩 (정규화된 위도, 경도)
        return Arrays.asList(
                random.nextGaussian() * 0.1, // 위도 기반
                random.nextGaussian() * 0.1  // 경도 기반
        );
    }

    private List<Double> generateRandomTagEmbedding() {
        // 1536차원 태그 임베딩 (OpenAI embedding 차원)
        return random.doubles(1536, -1.0, 1.0)
                .boxed()
                .toList();
    }

    private String formatVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return null;
        }
        return "[" + vector.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }

    // 테스트용 시나리오 메서드들
    @Transactional
    public void createCafeScenario() {
        log.info("Creating cafe test scenario...");

        // 카페 관련 spot들에 대한 임베딩 생성
        List<Long> cafeSpotIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        createTestEmbeddings(cafeSpotIds);
    }

    @Transactional
    public void createTourScenario() {
        log.info("Creating tour test scenario...");

        // 관광지 관련 spot들에 대한 임베딩 생성
        List<Long> tourSpotIds = Arrays.asList(100L, 101L, 102L, 103L, 104L);
        createTestEmbeddings(tourSpotIds);
    }

    public void validateEmbeddingData() {
        List<SpotEmbedding> all = getAllEmbeddings();
        log.info("Total embeddings: {}", all.size());

        List<SpotEmbedding> incomplete = getIncompleteEmbeddings();
        log.info("Incomplete embeddings: {}", incomplete.size());

        all.forEach(embedding -> {
            boolean locationValid = embedding.getLocationEmbedding() != null &&
                    embedding.getLocationEmbedding().size() == 2;
            boolean tagValid = embedding.getTagEmbedding() != null &&
                    embedding.getTagEmbedding().size() == 1536;

            log.debug("Spot {}: location={}, tag={}",
                    embedding.getSpotId(), locationValid, tagValid);
        });
    }
}