package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.mysql.entity.PostEntity;
import yunrry.flik.core.domain.model.PostType;

import java.util.List;

@Repository
public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {

    @Query("SELECT p FROM PostEntity p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:userId IS NULL OR p.userId = :userId) " +
            "ORDER BY p.createdAt DESC")
    Slice<PostEntity> findByConditions(
            @Param("type") PostType type,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT p FROM PostEntity p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:userId IS NULL OR p.userId = :userId) " +
            "ORDER BY p.createdAt DESC")
    List<PostEntity> findAllByConditions(
            @Param("type") PostType type,
            @Param("userId") Long userId
    );

}