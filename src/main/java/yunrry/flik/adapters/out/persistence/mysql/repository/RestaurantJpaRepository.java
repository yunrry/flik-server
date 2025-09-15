package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.mysql.entity.RestaurantEntity;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {

    @Query("SELECT r FROM RestaurantEntity r WHERE " +
            "(:category IS NULL OR r.category = :category) AND " +
            "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.description LIKE %:keyword%) AND " +
            "(:address IS NULL OR r.address LIKE %:address%)")
    Slice<RestaurantEntity> findByConditions(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("address") String address,
            Pageable pageable
    );
}