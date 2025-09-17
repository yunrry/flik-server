package yunrry.flik.adapters.in.web.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import yunrry.flik.core.domain.model.embedding.SpotSimilarity;
import yunrry.flik.core.domain.model.embedding.SpotEmbedding;
import yunrry.flik.core.service.embedding.SpotEmbeddingTestService;
import yunrry.flik.core.service.user.UserCategoryVectorService;
import yunrry.flik.core.service.user.UserSavedSpotService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test/embeddings")
@RequiredArgsConstructor
@Slf4j
public class SpotEmbeddingTestController {

    private final SpotEmbeddingTestService spotEmbeddingTestService;
    private final UserCategoryVectorService userCategoryVectorService;

    @PostMapping("/spots/{spotId}")
    public ResponseEntity<SpotEmbedding> createTestEmbedding(@PathVariable Long spotId) {
        try {
            SpotEmbedding embedding = spotEmbeddingTestService.createTestEmbedding(spotId);
            return ResponseEntity.ok(embedding);
        } catch (Exception e) {
            log.error("Failed to create test embedding for spot: {}", spotId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/spots/batch")
    public ResponseEntity<List<SpotEmbedding>> createTestEmbeddings(@RequestBody List<Long> spotIds) {
        try {
            List<SpotEmbedding> embeddings = spotEmbeddingTestService.createTestEmbeddings(spotIds);
            return ResponseEntity.ok(embeddings);
        } catch (Exception e) {
            log.error("Failed to create test embeddings for spots: {}", spotIds, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/spots/user-vectors")
    public ResponseEntity<Void> createUserCategoryVectors(@AuthenticationPrincipal Long userId) {
        try {
            userCategoryVectorService.recalculateAllUserVectors(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to create user category vectors for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/spots/{spotId}")
    public ResponseEntity<SpotEmbedding> updateTestEmbedding(@PathVariable Long spotId) {
        try {
            SpotEmbedding embedding = spotEmbeddingTestService.updateTestEmbedding(spotId);
            return ResponseEntity.ok(embedding);
        } catch (Exception e) {
            log.error("Failed to update test embedding for spot: {}", spotId, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/spots/{spotId}")
    public ResponseEntity<SpotEmbedding> getEmbedding(@PathVariable Long spotId) {
        Optional<SpotEmbedding> embedding = spotEmbeddingTestService.getEmbedding(spotId);
        return embedding.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/spots")
    public ResponseEntity<List<SpotEmbedding>> getAllEmbeddings() {
        List<SpotEmbedding> embeddings = spotEmbeddingTestService.getAllEmbeddings();
        return ResponseEntity.ok(embeddings);
    }

    @GetMapping("/spots/incomplete")
    public ResponseEntity<List<SpotEmbedding>> getIncompleteEmbeddings() {
        List<SpotEmbedding> embeddings = spotEmbeddingTestService.getIncompleteEmbeddings();
        return ResponseEntity.ok(embeddings);
    }

    @GetMapping("/spots/{spotId}/similar")
    public ResponseEntity<List<SpotSimilarity>> findSimilarSpots(
            @PathVariable Long spotId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal Long userId) {
        try {
            List<SpotSimilarity> similarSpots = spotEmbeddingTestService.findSimilarSpots(userId, limit);
            return ResponseEntity.ok(similarSpots);
        } catch (Exception e) {
            log.error("Failed to find similar spots for: {}", spotId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/spots/{spotId}")
    public ResponseEntity<Void> deleteEmbedding(@PathVariable Long spotId) {
        try {
            spotEmbeddingTestService.deleteEmbedding(spotId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete embedding for spot: {}", spotId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/spots")
    public ResponseEntity<Void> deleteAllTestEmbeddings() {
        try {
            spotEmbeddingTestService.deleteAllTestEmbeddings();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete all test embeddings", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/scenarios/cafe")
    public ResponseEntity<String> createCafeScenario() {
        try {
            spotEmbeddingTestService.createCafeScenario();
            return ResponseEntity.ok("Cafe test scenario created successfully");
        } catch (Exception e) {
            log.error("Failed to create cafe scenario", e);
            return ResponseEntity.internalServerError().body("Failed to create cafe scenario");
        }
    }

    @PostMapping("/scenarios/tour")
    public ResponseEntity<String> createTourScenario() {
        try {
            spotEmbeddingTestService.createTourScenario();
            return ResponseEntity.ok("Tour test scenario created successfully");
        } catch (Exception e) {
            log.error("Failed to create tour scenario", e);
            return ResponseEntity.internalServerError().body("Failed to create tour scenario");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateEmbeddingData() {
        try {
            spotEmbeddingTestService.validateEmbeddingData();
            return ResponseEntity.ok("Embedding data validation completed. Check logs for details.");
        } catch (Exception e) {
            log.error("Failed to validate embedding data", e);
            return ResponseEntity.internalServerError().body("Failed to validate embedding data");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<EmbeddingStats> getStats() {
        try {
            List<SpotEmbedding> all = spotEmbeddingTestService.getAllEmbeddings();
            List<SpotEmbedding> incomplete = spotEmbeddingTestService.getIncompleteEmbeddings();

            EmbeddingStats stats = EmbeddingStats.builder()
                    .totalEmbeddings(all.size())
                    .completeEmbeddings(all.size() - incomplete.size())
                    .incompleteEmbeddings(incomplete.size())
                    .completionRate(all.isEmpty() ? 0.0 :
                            (double)(all.size() - incomplete.size()) / all.size() * 100)
                    .build();

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get embedding stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 통계 데이터를 위한 DTO
    public static class EmbeddingStats {
        public final int totalEmbeddings;
        public final int completeEmbeddings;
        public final int incompleteEmbeddings;
        public final double completionRate;

        public EmbeddingStats(int totalEmbeddings, int completeEmbeddings,
                              int incompleteEmbeddings, double completionRate) {
            this.totalEmbeddings = totalEmbeddings;
            this.completeEmbeddings = completeEmbeddings;
            this.incompleteEmbeddings = incompleteEmbeddings;
            this.completionRate = completionRate;
        }

        public static EmbeddingStatsBuilder builder() {
            return new EmbeddingStatsBuilder();
        }

        public static class EmbeddingStatsBuilder {
            private int totalEmbeddings;
            private int completeEmbeddings;
            private int incompleteEmbeddings;
            private double completionRate;

            public EmbeddingStatsBuilder totalEmbeddings(int totalEmbeddings) {
                this.totalEmbeddings = totalEmbeddings;
                return this;
            }

            public EmbeddingStatsBuilder completeEmbeddings(int completeEmbeddings) {
                this.completeEmbeddings = completeEmbeddings;
                return this;
            }

            public EmbeddingStatsBuilder incompleteEmbeddings(int incompleteEmbeddings) {
                this.incompleteEmbeddings = incompleteEmbeddings;
                return this;
            }

            public EmbeddingStatsBuilder completionRate(double completionRate) {
                this.completionRate = completionRate;
                return this;
            }

            public EmbeddingStats build() {
                return new EmbeddingStats(totalEmbeddings, completeEmbeddings,
                        incompleteEmbeddings, completionRate);
            }
        }
    }
}