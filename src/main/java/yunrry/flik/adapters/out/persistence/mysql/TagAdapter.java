package yunrry.flik.adapters.out.persistence.mysql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import yunrry.flik.adapters.out.persistence.mysql.entity.TagEntity;
import yunrry.flik.adapters.out.persistence.mysql.repository.TagJpaRepository;
import yunrry.flik.core.domain.model.Tag;
import yunrry.flik.ports.out.repository.TagRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TagAdapter implements TagRepository {

    private final TagJpaRepository tagJpaRepository;

    @Override
    public Optional<Tag> findByName(String name) {
        return tagJpaRepository.findByName(name)
                .map(TagEntity::toDomain);
    }

    @Override
    @Transactional
    public Tag save(Tag tag) {
        try {
            TagEntity entity = TagEntity.from(tag);
            TagEntity savedEntity = tagJpaRepository.save(entity);
            log.debug("Tag saved successfully - name: {}, id: {}", savedEntity.getName(), savedEntity.getId());
            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate tag name violation - name: {}", tag.getName());
            throw new IllegalStateException("이미 존재하는 태그명입니다: " + tag.getName(), e);
        }
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return tagJpaRepository.findById(id)
                .map(TagEntity::toDomain);
    }

    @Override
    public List<Tag> findAll() {
        return tagJpaRepository.findAll().stream()
                .map(TagEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return tagJpaRepository.existsByName(name);
    }

    @Override
    public List<Tag> findByNameIn(List<String> names) {
        return tagJpaRepository.findByNameIn(names).stream()
                .map(TagEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        tagJpaRepository.deleteById(id);
        log.debug("Tag deleted by id: {}", id);
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        tagJpaRepository.deleteByName(name);
        log.debug("Tag deleted by name: {}", name);
    }

    // 추가 메서드들 (어댑터에서만 제공)
    public List<Tag> findByNameContaining(String pattern) {
        return tagJpaRepository.findByNameContaining(pattern).stream()
                .map(TagEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<Tag> findAllOrderByCreatedAtDesc() {
        return tagJpaRepository.findAllOrderByCreatedAtDesc().stream()
                .map(TagEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<Tag> findByEmbeddingIsNull() {
        return tagJpaRepository.findByEmbeddingIsNull().stream()
                .map(TagEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public Tag saveOrUpdate(Tag tag) {
        Optional<TagEntity> existingEntity = tagJpaRepository.findByName(tag.getName());

        if (existingEntity.isPresent()) {
            TagEntity entity = existingEntity.get();
            if (tag.getEmbedding() != null) {
                entity.updateEmbedding(tag.getEmbedding().toString());
                TagEntity savedEntity = tagJpaRepository.save(entity);
                log.debug("Tag embedding updated - name: {}", tag.getName());
                return savedEntity.toDomain();
            }
            return entity.toDomain();
        } else {
            return save(tag);
        }
    }
}