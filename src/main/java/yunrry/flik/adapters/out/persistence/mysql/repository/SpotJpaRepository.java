package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.mysql.entity.BaseSpotEntity;
import yunrry.flik.core.domain.model.card.Spot;

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

    List<BaseSpotEntity> findAllById(Iterable<Long> ids);

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


    @Query("SELECT s FROM BaseSpotEntity s WHERE s.labelDepth2 IN :subcategories")
    List<BaseSpotEntity> findByLabelDepth2In(@Param("subcategories") List<String> subcategories);


    @Query("SELECT s FROM BaseSpotEntity s WHERE s.id IN :spotIds AND s.labelDepth2 IN :labelDepth2Categories")
    List<BaseSpotEntity> findByIdsAndLabelDepth2In(
            @Param("spotIds") List<Long> spotIds,
            @Param("labelDepth2Categories") List<String> labelDepth2Categories);


    @Query("SELECT s.id FROM BaseSpotEntity s WHERE s.id IN :spotIds AND s.labelDepth2 IN :labelDepth2Categories")
    List<Long> findIdsByIdsAndLabelDepth2In(List<Long> spotIds, List<String> labelDepth2Categories);

    @Query("SELECT s.id FROM BaseSpotEntity s WHERE s.id IN :spotIds AND s.labelDepth2 IN :labelDepth2Categories AND s.regnCd = :regnCd AND s.signguCd = :signguCd")
    List<Long> findIdsByIdsAndLabelDepth2InAndRegnCdAndSignguCd(List<Long> spotIds, List<String> labelDepth2Categories,
                                            @Param("regnCd") String regnCd,
                                            @Param("signguCd") String signguCd);

    @Query("SELECT s.id FROM BaseSpotEntity s WHERE s.id IN :spotIds AND s.labelDepth2 IN :labelDepth2Categories AND s.regnCd = :regnCd")
    List<Long> findIdsByIdsAndLabelDepth2InAndRegnCd(List<Long> spotIds, List<String> labelDepth2Categories,
                                                                @Param("regnCd") String regnCd);


    @Query(value = "SELECT * FROM spots ORDER BY RAND() LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<BaseSpotEntity> findRandomSpots(@Param("offset") int offset, @Param("limit") int limit);

}
