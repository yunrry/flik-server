package yunrry.flik.adapters.out.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.entity.SpotEntity;
import yunrry.flik.adapters.out.persistence.entity.SpotEntity;

@Repository
public interface SpotJpaRepository extends JpaRepository<SpotEntity, Long> {
    @Query("SELECT r FROM SpotEntity r WHERE " +
            "(:category IS NULL OR r.category = :category) AND " +
            "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.description LIKE %:keyword%) AND " +
            "(:address IS NULL OR r.address LIKE %:address%)")
    Slice<SpotEntity> findByConditions(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("address") String address,
            Pageable pageable
    );
}
