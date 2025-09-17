package yunrry.flik.core.service.plan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
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
        return Mono.fromCallable(() -> keywords.stream()
                        .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                        .map(String::trim)
                        .toList())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::saveTagIfNotExists)
                .then();
    }

    private Mono<Void> saveTagIfNotExists(String tagName) {
        return Mono.fromCallable(() -> tagRepository.existsByName(tagName))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return createAndSaveTag(tagName);
                    } else {
                        return updateTagIfNoEmbedding(tagName);
                    }
                });
    }

    private Mono<Void> createAndSaveTag(String tagName) {
        return embeddingService.createEmbedding(tagName)
                .flatMap(embedding -> Mono.fromRunnable(() -> {
                    Tag tag = Tag.of(tagName, embedding);
                    tagRepository.save(tag);
                    log.info("Saved new tag with embedding: {}", tagName);
                }).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }

    private Mono<Void> updateTagIfNoEmbedding(String tagName) {
        return Mono.fromCallable(() -> tagRepository.findByName(tagName))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalTag -> {
                    if (optionalTag.isPresent() && !optionalTag.get().hasEmbedding()) {
                        return embeddingService.createEmbedding(tagName)
                                .flatMap(embedding -> Mono.fromRunnable(() -> {
                                    Tag updatedTag = optionalTag.get().withEmbedding(embedding);
                                    tagRepository.save(updatedTag);
                                    log.info("Updated tag with embedding: {}", tagName);
                                }).subscribeOn(Schedulers.boundedElastic()));
                    }
                    return Mono.empty();
                })
                .then();
    }

    public Mono<List<Tag>> findTagsWithoutEmbedding() {
        return Mono.fromCallable(() -> {
                    if (tagRepository instanceof yunrry.flik.adapters.out.persistence.mysql.TagAdapter adapter) {
                        return adapter.findByEmbeddingIsNull();
                    }
                    return List.<Tag>of();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> generateMissingEmbeddings() {
        return findTagsWithoutEmbedding()
                .flatMapMany(Flux::fromIterable)
                .flatMap(tag -> embeddingService.createEmbedding(tag.getName())
                        .flatMap(embedding -> Mono.fromRunnable(() -> {
                            Tag updatedTag = tag.withEmbedding(embedding);
                            tagRepository.save(updatedTag);
                            log.info("Generated missing embedding for tag: {}", tag.getName());
                        }).subscribeOn(Schedulers.boundedElastic())))
                .then();
    }
}