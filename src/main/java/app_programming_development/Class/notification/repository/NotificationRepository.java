package app_programming_development.Class.notification.repository;

import app_programming_development.Class.notification.entity.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    Page<Notifications> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Long countByUser_IdAndIsRead(Long userId, boolean isRead);
}
