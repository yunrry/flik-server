package yunrry.flik.ports.out.repository;

import yunrry.flik.core.domain.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    /**
     * 태그명으로 태그 조회
     */
    Optional<Tag> findByName(String name);

    /**
     * 태그 저장
     */
    Tag save(Tag tag);

    /**
     * 태그 ID로 조회
     */
    Optional<Tag> findById(Long id);

    /**
     * 모든 태그 조회
     */
    List<Tag> findAll();

    /**
     * 태그명으로 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 태그명 목록으로 태그들 조회
     */
    List<Tag> findByNameIn(List<String> names);

    /**
     * 태그 삭제
     */
    void deleteById(Long id);

    /**
     * 태그명으로 삭제
     */
    void deleteByName(String name);
}
