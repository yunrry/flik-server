package yunrry.flik.core.domain.model.embedding;

public record EmbeddingStats(
        long totalSpots,
        long spotsWithTagEmbedding,
        long spotsWithLocationEmbedding,
        long spotsWithoutTagEmbedding,
        double coveragePercentage
) {}