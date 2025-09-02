package yunrry.flik.adapters.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.entity.RestaurantEntity;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {
}