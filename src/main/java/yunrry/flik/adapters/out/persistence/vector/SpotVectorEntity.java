package yunrry.flik.adapters.out.persistence.vector;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

// PostgreSQL 벡터 전용 엔티티
@Entity
@Getter
@Table(name = "spot_vectors")
public class SpotVectorEntity {

    @Id
    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "location_embedding", columnDefinition = "vector(2)")
    private String locationEmbedding;

    @Column(name = "tag_embedding", columnDefinition = "vector(1536)")
    private String tagEmbedding;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}