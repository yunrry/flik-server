package yunrry.flik.adapters.out.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.entity.BaseSpotEntity;
import yunrry.flik.adapters.out.persistence.entity.BaseSpotEntity;

import java.util.List;

@Repository
public interface SpotJpaRepository extends JpaRepository<BaseSpotEntity, Long> {
    @Query("SELECT r FROM BaseSpotEntity r WHERE " +
            "(:category IS NULL OR r.category = :category) AND " +
            "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.description LIKE %:keyword%) AND " +
            "(:address IS NULL OR r.address LIKE %:address%)")
    Slice<BaseSpotEntity> findByConditions(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("address") String address,
            Pageable pageable
    );

    @Query("SELECT s FROM BaseSpotEntity s WHERE s.labelDepth2 IN :subcategories AND s.regnCd = :regnCd AND s.signguCd = :signguCd")
    List<BaseSpotEntity> findByLabelDepth2InAndRegnCdAndSignguCd(
            @Param("subcategories") List<String> subcategories,
            @Param("regnCd") String regnCd,
            @Param("signguCd") String signguCd);

    @Query("SELECT s FROM BaseSpotEntity s WHERE s.labelDepth2 IN :subcategories AND s.regnCd = :regnCd AND s.signguCd = :signguCd")
    Page<BaseSpotEntity> findPageByLabelDepth2InAndRegnCdAndSignguCd(
            @Param("subcategories") List<String> subcategories,
            @Param("regnCd") String regnCd,
            @Param("signguCd") String signguCd,
            Pageable pageable);

    @Query("SELECT s FROM BaseSpotEntity s WHERE s.labelDepth2 IN :subcategories AND s.regnCd = :regnCd AND s.signguCd = :signguCd")
    Slice<BaseSpotEntity> findSliceByLabelDepth2InAndRegnCdAndSignguCd(
            @Param("subcategories") List<String> subcategories,
            @Param("regnCd") String regnCd,
            @Param("signguCd") String signguCd,
            Pageable pageable);

    @Query("SELECT s FROM BaseSpotEntity s WHERE s.labelDepth2 IN :subcategories AND s.regnCd = :regnCd AND s.signguCd = :signguCd ORDER BY s.rating DESC")
    List<BaseSpotEntity> findByLabelDepth2InAndRegnCdAndSignguCdOrderByRatingDesc(
            @Param("subcategories") List<String> subcategories,
            @Param("regnCd") String regnCd,
            @Param("signguCd") String signguCd,
            Pageable pageable);
}
