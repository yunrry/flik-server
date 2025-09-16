package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import yunrry.flik.core.domain.model.Tag;
import yunrry.flik.core.service.embedding.OpenAIEmbeddingService;
import yunrry.flik.ports.out.repository.TagRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final OpenAIEmbeddingService embeddingService;

    @Transactional
    public Mono<Void> saveKeywords(List<String> keywords) {
        return Mono.fromCallable(() -> {
                    for (String keyword : keywords) {
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            saveTagIfNotExists(keyword.trim());
                        }
                    }
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private void saveTagIfNotExists(String tagName) {
        if (!tagRepository.existsByName(tagName)) {
            // 임베딩 생성 후 태그 저장
            embeddingService.createEmbedding(tagName)
                    .subscribe(embedding -> {
                        Tag tag = Tag.of(tagName, embedding);
                        tagRepository.save(tag);
                        log.info("Saved new tag with embedding: {}", tagName);
                    });
        } else {
            // 태그는 존재하지만 임베딩이 없는 경우
            Optional<Tag> existingTag = tagRepository.findByName(tagName);
            if (existingTag.isPresent() && !existingTag.get().hasEmbedding()) {
                embeddingService.createEmbedding(tagName)
                        .subscribe(embedding -> {
                            Tag updatedTag = existingTag.get().withEmbedding(embedding);
                            tagRepository.save(updatedTag);
                            log.info("Updated tag with embedding: {}", tagName);
                        });
            }
        }
    }

    public Mono<List<Tag>> findTagsWithoutEmbedding() {
        return Mono.fromCallable(() -> {
                    // 어댑터의 추가 메서드 사용
                    if (tagRepository instanceof yunrry.flik.adapters.out.persistence.mysql.TagAdapter) {
                        yunrry.flik.adapters.out.persistence.mysql.TagAdapter adapter =
                                (yunrry.flik.adapters.out.persistence.mysql.TagAdapter) tagRepository;
                        return adapter.findByEmbeddingIsNull();
                    }
                    return List.<Tag>of();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> generateMissingEmbeddings() {
        return findTagsWithoutEmbedding()
                .flatMap(tags -> {
                    for (Tag tag : tags) {
                        embeddingService.createEmbedding(tag.getName())
                                .subscribe(embedding -> {
                                    Tag updatedTag = tag.withEmbedding(embedding);
                                    tagRepository.save(updatedTag);
                                    log.info("Generated missing embedding for tag: {}", tag.getName());
                                });
                    }
                    return Mono.empty();
                });
    }
}