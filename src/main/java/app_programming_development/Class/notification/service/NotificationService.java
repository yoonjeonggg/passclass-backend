package app_programming_development.Class.notification.service;

import app_programming_development.Class.dto.notification.response.NotificationResponse;
import app_programming_development.Class.dto.notification.response.UnreadCountResponse;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.exceptions.forbidden.NotNotificationOwnerException;
import app_programming_development.Class.exceptions.notFound.NotificationNotFoundException;
import app_programming_development.Class.notification.entity.Notifications;
import app_programming_development.Class.notification.repository.NotificationRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 1800000L; // 30분

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(int page, int size) {
        Users currentUser = securityUtils.getCurrentUser();
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(page, size))
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Users currentUser = securityUtils.getCurrentUser();
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);

        if (!Objects.equals(notification.getUser().getId(), currentUser.getId())) {
            throw new NotNotificationOwnerException();
        }

        notification.setRead(true);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        Users currentUser = securityUtils.getCurrentUser();
        Long count = notificationRepository.countByUser_IdAndIsRead(currentUser.getId(), false);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Transactional
    public void markAllAsRead() {
        Users currentUser = securityUtils.getCurrentUser();
        int updated = notificationRepository.markAllAsReadByUserId(currentUser.getId());
        log.info("All notifications marked as read: userId={}, count={}", currentUser.getId(), updated);
    }

    @Transactional
    public void createNotification(Users recipient, NotificationType type, String content) {
        Notifications notification = Notifications.builder()
                .user(recipient)
                .type(type)
                .content(content)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification sent: recipientId={}, type={}", recipient.getId(), type);

        SseEmitter emitter = emitters.get(recipient.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(NotificationResponse.from(notification)));
            } catch (IOException e) {
                emitters.remove(recipient.getId());
            }
        }
    }

    public SseEmitter subscribe() {
        Users currentUser = securityUtils.getCurrentUser();
        Long userId = currentUser.getId();

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        log.debug("SSE subscribed: userId={}", userId);
        return emitter;
    }
}
