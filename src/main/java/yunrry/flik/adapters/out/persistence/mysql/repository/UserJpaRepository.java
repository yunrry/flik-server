package yunrry.flik.adapters.out.persistence.mysql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunrry.flik.adapters.out.persistence.mysql.entity.UserEntity;
import yunrry.flik.core.domain.model.AuthProvider;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.authProvider = :provider AND u.providerId = :providerId")
    Optional<UserEntity> findByProviderAndProviderId(
            @Param("provider") AuthProvider provider,
            @Param("providerId") String providerId
    );

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.authProvider = :provider")
    Optional<UserEntity> findByEmailAndProvider(
            @Param("email") String email,
            @Param("provider") AuthProvider provider
    );


    @Query("SELECT u.nickname FROM UserEntity u WHERE u.id = :id")
    Optional<String> findNicknameById(@Param("id") Long id);
}