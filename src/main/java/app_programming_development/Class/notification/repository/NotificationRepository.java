package app_programming_development.Class.notification.repository;

import app_programming_development.Class.notification.entity.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    Page<Notifications> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notifications> findByUser_IdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead, Pageable pageable);
    Long countByUser_IdAndIsRead(Long userId, boolean isRead);

    @Modifying
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
