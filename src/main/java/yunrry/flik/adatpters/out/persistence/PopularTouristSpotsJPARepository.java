//package yunrry.flik.infrastructure.adapter.out.persistence;
//
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import yys.safewalk.domain.model.PopularTouristSpots;
//import yys.safewalk.entity.AdministrativeLegalDongs;
//import yys.safewalk.entity.PopularTouristSpotsEntity;
//import java.util.Optional;
//import java.util.List;
//
//@Repository
//public interface PopularTouristSpotsJPARepository extends JpaRepository<PopularTouristSpotsEntity, Long> {
//
//    List<PopularTouristSpotsEntity> findByLongitudeIsNullOrLatitudeIsNull();
//
//    List<PopularTouristSpotsEntity> findBySidoNameAndSigunguName(String sidoName, String sigunguName);
//
//    @Query("SELECT p FROM PopularTouristSpotsEntity p " +
//            "WHERE p.latitude BETWEEN :swLat AND :neLat " +
//            "AND p.longitude BETWEEN :swLng AND :neLng " +
//            "AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
//    List<PopularTouristSpotsEntity> findByLatitudeBetweenAndLongitudeBetween(
//            @Param("swLat") Double swLat,
//            @Param("neLat") Double neLat,
//            @Param("swLng") Double swLng,
//            @Param("neLng") Double neLng
//    );
//
//    /**
//     * 시군구명이 null인 관광지 조회
//     */
//    List<PopularTouristSpotsEntity> findBySigunguNameIsNull();
//
//    /**
//     * 시도코드와 모드로 관광지 조회 (rank 순으로 정렬)
//     */
//    @Query("SELECT p FROM PopularTouristSpotsEntity p " +
//            "WHERE p.sidoCode = :sidoCode AND p.mode = :mode " +
//            "ORDER BY p.rank ASC")
//    List<PopularTouristSpotsEntity> findBySidoCodeAndMode(
//            @Param("sidoCode") String sidoCode,
//            @Param("mode") String mode
//    );
//
//    Optional<PopularTouristSpotsEntity> findByTouristSpotId(String touristSpotId);
//
//    List<PopularTouristSpotsEntity> findAllByTouristSpotId(String touristSpotId);
//
//    @Query("SELECT a FROM PopularTouristSpotsEntity a WHERE a.spotName LIKE :query% ORDER BY a.spotName")
//    List<PopularTouristSpotsEntity> findBySpotNameStartingWith(String query, PageRequest of);
//}