package yunrry.flik.core.service.plan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import yunrry.flik.adapters.out.persistence.mysql.TagAdapter;
import yunrry.flik.core.domain.model.Tag;
import yunrry.flik.core.service.embedding.OpenAIEmbeddingService;
import yunrry.flik.ports.out.repository.TagRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TagService 테스트")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagAdapter tagAdapter;

    @Mock
    private OpenAIEmbeddingService embeddingService;

    @InjectMocks
    private TagService tagService;

    private List<String> sampleKeywords;
    private Tag sampleTag;
    private List<Double> sampleEmbedding;


    @BeforeEach
    void setUp() {
        reset(tagRepository, embeddingService); // Mock 초기화 추가
        sampleKeywords = Arrays.asList("카페", "브런치", "데이트");
        sampleEmbedding = Arrays.asList(0.1, 0.2, 0.3);
        sampleTag = Tag.of("카페", sampleEmbedding);
    }

//    @Test
//    @DisplayName("키워드 저장 성공 - 새로운 태그들")
//    void saveKeywords_NewTags_Success() {
//        // Given
//        when(tagRepository.existsByName(anyString())).thenReturn(false);
//        when(embeddingService.createEmbedding(anyString()))
//                .thenReturn(Mono.just(sampleEmbedding));
//        when(tagRepository.save(any(Tag.class))).thenReturn(sampleTag);
//
//        // When & Then
//        StepVerifier.create(tagService.saveKeywords(sampleKeywords))
//                .verifyComplete();
//
//        verify(tagRepository, times(3)).existsByName(anyString());
//        verify(embeddingService, times(3)).createEmbedding(anyString());
//        verify(tagRepository, times(3)).save(any(Tag.class));
//    }

//    @Test
//    @DisplayName("키워드 저장 성공 - 기존 태그 존재")
//    void saveKeywords_ExistingTags_Success() {
//        // Given
//        Tag existingTagWithEmbedding = Tag.of("카페", sampleEmbedding);
//
//        when(tagRepository.existsByName("카페")).thenReturn(true);
//        when(tagRepository.findByName("카페")).thenReturn(Optional.of(existingTagWithEmbedding));
//
//        // When & Then
//        StepVerifier.create(tagService.saveKeywords(Arrays.asList("카페")))
//                .verifyComplete();
//
//        verify(tagRepository).existsByName("카페");
//        verify(tagRepository).findByName("카페");
//        verify(embeddingService, never()).createEmbedding(anyString());
//        verify(tagRepository, never()).save(any(Tag.class));
//    }

//    @Test
//    @DisplayName("키워드 저장 성공 - 임베딩 없는 기존 태그 업데이트")
//    void saveKeywords_ExistingTagWithoutEmbedding_Success() {
//        // Given
//        Tag tagWithoutEmbedding = Tag.of("카페", null);
//
//        when(tagRepository.existsByName("카페")).thenReturn(true);
//        when(tagRepository.findByName("카페")).thenReturn(Optional.of(tagWithoutEmbedding));
//        when(embeddingService.createEmbedding("카페"))
//                .thenReturn(Mono.just(sampleEmbedding));
//        when(tagRepository.save(any(Tag.class))).thenReturn(sampleTag);
//
//        // When & Then
//        StepVerifier.create(tagService.saveKeywords(Arrays.asList("카페")))
//                .verifyComplete();
//
//        verify(tagRepository).existsByName("카페");
//        verify(tagRepository).findByName("카페");
//    }


    @Test
    @DisplayName("임베딩 없는 태그 조회 성공")
    void findTagsWithoutEmbedding_Success() {
        // Given
        List<Tag> tagsWithoutEmbedding = Arrays.asList(
                Tag.of("태그1", null),
                Tag.of("태그2", null)
        );

        TagService tagServiceWithAdapter = new TagService(tagAdapter, embeddingService);
        when(tagAdapter.findByEmbeddingIsNull()).thenReturn(tagsWithoutEmbedding);

        // When & Then
        StepVerifier.create(tagServiceWithAdapter.findTagsWithoutEmbedding())
                .expectNext(tagsWithoutEmbedding)
                .verifyComplete();
    }

    @Test
    @DisplayName("임베딩 없는 태그 조회 - TagAdapter가 아닌 경우")
    void findTagsWithoutEmbedding_NotTagAdapter_ReturnsEmpty() {
        // Given - tagRepository가 TagAdapter가 아님

        // When & Then
        StepVerifier.create(tagService.findTagsWithoutEmbedding())
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    @DisplayName("누락된 임베딩 생성 성공")
    void generateMissingEmbeddings_Success() {
        // Given
        List<Tag> tagsWithoutEmbedding = Arrays.asList(
                Tag.of("태그1", null),
                Tag.of("태그2", null)
        );

        TagService tagServiceWithAdapter = new TagService(tagAdapter, embeddingService);
        when(tagAdapter.findByEmbeddingIsNull()).thenReturn(tagsWithoutEmbedding);
        when(embeddingService.createEmbedding(anyString()))
                .thenReturn(Mono.just(sampleEmbedding));
        when(tagAdapter.save(any(Tag.class))).thenReturn(sampleTag);

        // When & Then
        StepVerifier.create(tagServiceWithAdapter.generateMissingEmbeddings())
                .verifyComplete();
    }

    @Test
    @DisplayName("누락된 임베딩 생성 - 빈 리스트")
    void generateMissingEmbeddings_EmptyList_Success() {
        // Given
        TagService tagServiceWithAdapter = new TagService(tagAdapter, embeddingService);
        when(tagAdapter.findByEmbeddingIsNull()).thenReturn(List.of());

        // When & Then
        StepVerifier.create(tagServiceWithAdapter.generateMissingEmbeddings())
                .verifyComplete();

        verify(embeddingService, never()).createEmbedding(anyString());
        verify(tagAdapter, never()).save(any(Tag.class));
    }

//    @Test
//    @DisplayName("키워드 저장 실패 - 임베딩 생성 실패")
//    void saveKeywords_EmbeddingCreationFails() {
//        // Given
//        when(tagRepository.existsByName("카페")).thenReturn(false);
//        when(embeddingService.createEmbedding("카페"))
//                .thenReturn(Mono.error(new RuntimeException("Embedding creation failed")));
//
//        // When & Then - 메인 플로우는 성공하지만 비동기 에러 발생
//        StepVerifier.create(tagService.saveKeywords(Arrays.asList("카페")))
//                .verifyComplete();
//
//        // 비동기 작업 대기 후 검증
//        verify(tagRepository, timeout(1000)).existsByName("카페");
//        // 에러는 로그에서 확인 가능: "Operator called default onErrorDropped"
//    }

    @Test
    @DisplayName("키워드 저장 실패 - 레포지토리 조회 실패")
    void saveKeywords_RepositoryFailure() {
        // Given
        when(tagRepository.existsByName("카페"))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        StepVerifier.create(tagService.saveKeywords(Arrays.asList("카페")))
                .expectError(RuntimeException.class)
                .verify();
    }
}