package app_programming_development.Class.auth.repository;

import app_programming_development.Class.auth.entity.RefreshTokens;
import app_programming_development.Class.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, Long> {
    void deleteByUser(Users user);
    Optional<RefreshTokens> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshTokens rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
