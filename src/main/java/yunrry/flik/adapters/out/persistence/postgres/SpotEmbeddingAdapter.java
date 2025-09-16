package yunrry.flik.adapters.out.persistence.postgres;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional(readOnly = true)
public class SpotEmbeddingAdapter implements SpotEmbeddingRepository {

    private final SpotEmbeddingJpaRepository spotEmbeddingJpaRepository;

    @Override
    public Optional<SpotEmbedding> findBySpotId(Long spotId) {
        return spotEmbeddingJpaRepository.findBySpotId(spotId)
                .map(SpotEmbeddingEntity::toDomain);
    }

    @Override
    @Transactional
    public SpotEmbedding save(SpotEmbedding spotEmbedding) {
        SpotEmbeddingEntity entity;

        if (spotEmbedding.getId() != null) {
            entity = spotEmbeddingJpaRepository.findById(spotEmbedding.getId())
                    .orElse(SpotEmbeddingEntity.fromDomain(spotEmbedding));
            entity.updateEmbeddings(
                    spotEmbedding.getLocationEmbeddingAsString(),
                    spotEmbedding.getTagEmbeddingAsString()
            );
        } else {
            entity = SpotEmbeddingEntity.fromDomain(spotEmbedding);
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
    public List<Long> findSimilarSpots(List<Long> spotIds,
                                       String locationVector,
                                       String tagVector,
                                       double locationWeight,
                                       double tagWeight,
                                       int limit) {
        List<Object[]> results = spotEmbeddingJpaRepository.findSimilarSpots(
                spotIds, locationVector, tagVector, locationWeight, tagWeight, limit);

        return results.stream()
                .map(row -> ((SpotEmbeddingEntity) row[0]).getSpotId())
                .collect(Collectors.toList());
    }

    @Override
    public List<SpotEmbedding> findIncompleteEmbeddings() {
        return spotEmbeddingJpaRepository.findIncompleteEmbeddings().stream()
                .map(SpotEmbeddingEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBySpotId(Long spotId) {
        spotEmbeddingJpaRepository.deleteBySpotId(spotId);
        log.debug("Deleted spot embedding for spot: {}", spotId);
    }

    @Override
    public List<SpotEmbedding> findAll() {
        return spotEmbeddingJpaRepository.findAll().stream()
                .map(SpotEmbeddingEntity::toDomain)
                .collect(Collectors.toList());
    }
}