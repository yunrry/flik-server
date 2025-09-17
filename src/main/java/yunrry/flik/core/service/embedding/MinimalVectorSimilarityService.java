package yunrry.flik.core.service.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinimalVectorSimilarityService {

    private final JdbcTemplate vectorJdbcTemplate; // PostgreSQL

    public List<Long> findSimilarSpotIds(Long userId, String category, List<Long> spotIds, int limit) {
        String sql = "SELECT spot_id FROM find_similar_spots(?, ?, ?, ?)";

        try {
            return vectorJdbcTemplate.queryForList(sql, Long.class,
                    userId, category, spotIds.toArray(Long[]::new), limit);
        } catch (Exception e) {
            log.error("Vector similarity search failed: {}", e.getMessage());
            return spotIds.subList(0, Math.min(limit, spotIds.size()));
        }
    }
}
