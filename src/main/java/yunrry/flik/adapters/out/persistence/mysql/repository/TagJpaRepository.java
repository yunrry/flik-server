package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yunrry.flik.adapters.out.persistence.mysql.entity.TagEntity;

import java.util.List;
import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<TagEntity, Long> {

    /**
     * 태그명으로 태그 조회
     */
    Optional<TagEntity> findByName(String name);

    /**
     * 태그명으로 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 태그명 목록으로 태그들 조회
     */
    List<TagEntity> findByNameIn(List<String> names);

    /**
     * 태그명으로 삭제
     */
    void deleteByName(String name);

    /**
     * 태그명 패턴으로 검색 (LIKE 검색)
     */
    @Query("SELECT t FROM TagEntity t WHERE t.name LIKE %:pattern%")
    List<TagEntity> findByNameContaining(@Param("pattern") String pattern);

    /**
     * 최근 생성된 태그들 조회
     */
    @Query("SELECT t FROM TagEntity t ORDER BY t.createdAt DESC")
    List<TagEntity> findAllOrderByCreatedAtDesc();

    /**
     * 임베딩이 없는 태그들 조회
     */
    @Query("SELECT t FROM TagEntity t WHERE t.embedding IS NULL")
    List<TagEntity> findByEmbeddingIsNull();
}