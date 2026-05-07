package app_programming_development.Class.auth.repository;

import app_programming_development.Class.auth.entity.RefreshTokens;
import app_programming_development.Class.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokens, Long> {
    void deleteByUser(Users user);
    Optional<RefreshTokens> findByToken(String token);
}
