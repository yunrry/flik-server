package yunrry.flik.core.domain.model.embedding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public record UserVectorStats(
        Long userId,
        long vectorCount,
        long totalCategories,
        double coveragePercentage
) {}
