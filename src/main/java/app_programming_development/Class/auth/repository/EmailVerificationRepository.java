package app_programming_development.Class.auth.repository;

import app_programming_development.Class.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}
