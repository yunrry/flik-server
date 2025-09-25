package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yunrry.flik.adapters.out.persistence.mysql.entity.SigunguCoordinate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SigunguCoordinateRepository extends JpaRepository<SigunguCoordinate, String> {

    SigunguCoordinate findBySigCd(String sigCd);

    @Query(value = """
        SELECT sig_cd 
        FROM sigungu_coordinate 
        WHERE (6371 * ACOS(COS(RADIANS(:centerY)) * COS(RADIANS(y)) * 
               COS(RADIANS(x) - RADIANS(:centerX)) + SIN(RADIANS(:centerY)) * 
               SIN(RADIANS(y)))) <= :radiusKm
        """, nativeQuery = true)
    List<String> findRegionsWithinRadius(@Param("centerX") double centerX,
                                         @Param("centerY") double centerY,
                                         @Param("radiusKm") int radiusKm);
}