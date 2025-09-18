package yunrry.flik.adapters.out.persistence.postgres;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.core.domain.model.embedding.EmbeddingStats;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.adapters.out.persistence.postgres.entity.SpotEmbeddingEntity;
import yunrry.flik.adapters.out.persistence.postgres.repository.SpotEmbeddingJpaRepository;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.ports.out.repository.SpotEmbeddingRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotEmbeddingAdapter implements SpotEmbeddingRepository {

    private final SpotEmbeddingJpaRepository spotEmbeddingJpaRepository;

    @Override
    public Optional<SpotEmbedding> findBySpotId(Long spotId) {
        return spotEmbeddingJpaRepository.findBySpotId(spotId)
                .map(SpotEmbeddingEntity::toDomain);
    }

    @Override
    public SpotEmbedding save(SpotEmbedding spotEmbedding) {
        SpotEmbeddingEntity entity;

        if (spotEmbedding.getId() != null) {
            entity = spotEmbeddingJpaRepository.findById(spotEmbedding.getId())
                    .orElse(SpotEmbeddingEntity.fromDomain(spotEmbedding));
            entity.updateEmbeddings(
                    spotEmbedding.getLocationEmbedding(),
                    spotEmbedding.getTagEmbedding()
            );
        } else {
            // spot_id로 기존 엔티티 확인 (upsert 방식)
            entity = spotEmbeddingJpaRepository.findBySpotId(spotEmbedding.getSpotId())
                    .map(existing -> {
                        existing.updateEmbeddings(
                                spotEmbedding.getLocationEmbedding(),
                                spotEmbedding.getTagEmbedding()
                        );
                        return existing;
                    })
                    .orElse(SpotEmbeddingEntity.fromDomain(spotEmbedding));
        }

        SpotEmbeddingEntity savedEntity = spotEmbeddingJpaRepository.save(entity);
        log.debug("Saved spot embedding for spot: {}", savedEntity.getSpotId());

        return savedEntity.toDomain();
    }

    @Override
    public List<SpotEmbedding> findBySpotIds(List<Long> spotIds) {
        return spotEmbeddingJpaRepository.findBySpotIdIn(spotIds).stream()
                .map(SpotEmbeddingEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SpotSimilarity> findSimilarSpotsByUserPreference(Long userId,
                                                                 String category,
                                                                 List<Long> spotIds,
                                                                 int limit) {
        try {
            List<Object[]> results = spotEmbeddingJpaRepository.findSimilarSpotsByUserPreference(
                    userId, category, spotIds.toArray(Long[]::new), limit);

            return results.stream()
                    .map(row -> new SpotSimilarity(
                            ((Number) row[0]).longValue(),      // spot_id
                            ((Number) row[1]).doubleValue()     // similarity
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find similar spots by user preference: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Long> findSpotIdsWithTagEmbedding(List<Long> spotIds) {
        return spotEmbeddingJpaRepository.findSpotIdsWithTagEmbedding(spotIds);
    }

    @Override
    public List<Long> findSpotIdsWithoutTagEmbedding(List<Long> spotIds) {
        return spotEmbeddingJpaRepository.findSpotIdsWithoutTagEmbedding(spotIds);
    }

    @Override
    public List<SpotEmbedding> findIncompleteEmbeddings() {
        return spotEmbeddingJpaRepository.findIncompleteEmbeddings().stream()
                .map(SpotEmbeddingEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public EmbeddingStats getEmbeddingStats() {
        long totalSpots = spotEmbeddingJpaRepository.count();
        long spotsWithTagEmbedding = spotEmbeddingJpaRepository.countSpotsWithTagEmbedding();
        long spotsWithLocationEmbedding = spotEmbeddingJpaRepository.countSpotsWithLocationEmbedding();

        return new EmbeddingStats(
                totalSpots,
                spotsWithTagEmbedding,
                spotsWithLocationEmbedding,
                totalSpots - spotsWithTagEmbedding,
                totalSpots > 0 ? (double) spotsWithTagEmbedding / totalSpots * 100 : 0.0
        );
    }

    @Override
    public void deleteBySpotId(Long spotId) {
        spotEmbeddingJpaRepository.deleteBySpotId(spotId);
        log.debug("Deleted spot embedding for spot: {}", spotId);
    }

    @Override
    public boolean existsBySpotId(Long spotId) {
        return spotEmbeddingJpaRepository.existsBySpotId(spotId);
    }

    @Override
    public List<SpotEmbedding> findAll() {
        return spotEmbeddingJpaRepository.findAll().stream()
                .map(SpotEmbeddingEntity::toDomain)
                .collect(Collectors.toList());
    }

    // DTOs


}