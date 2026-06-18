package app_programming_development.Class.payment.repository;

import app_programming_development.Class.enums.PaymentStatus;
import app_programming_development.Class.payment.entity.Payments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, Long> {
    Optional<Payments> findByOrderId(String orderId);
    Optional<Payments> findByUserIdAndLectureIdAndStatus(Long userId, Long lectureId, PaymentStatus status);
    Page<Payments> findByUserId(Long userId, Pageable pageable);
    boolean existsByUserIdAndLectureIdAndStatusIn(Long userId, Long lectureId, List<PaymentStatus> statuses);
}
