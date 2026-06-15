package app_programming_development.Class.scheduler;

import app_programming_development.Class.auth.repository.EmailVerificationRepository;
import app_programming_development.Class.auth.repository.RefreshTokenRepository;
import app_programming_development.Class.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationRepository notificationRepository;

    // 매 시간 만료된 이메일 인증 코드 삭제
    @Scheduled(cron = "${scheduler.cleanup.email-verification.cron}")
    @Transactional
    public void cleanupExpiredEmailVerifications() {
        int deleted = emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[Scheduler] 만료된 이메일 인증 코드 {}건 삭제", deleted);
        }
    }

    // 매일 새벽 3시 만료된 리프레시 토큰 삭제
    @Scheduled(cron = "${scheduler.cleanup.refresh-token.cron}")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[Scheduler] 만료된 리프레시 토큰 {}건 삭제", deleted);
        }
    }

    // 매주 일요일 새벽 4시 30일 이전에 읽은 알림 삭제
    @Scheduled(cron = "${scheduler.cleanup.notification.cron}")
    @Transactional
    public void cleanupOldReadNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldReadNotifications(cutoff);
        if (deleted > 0) {
            log.info("[Scheduler] 오래된 읽은 알림 {}건 삭제 (기준: {})", deleted, cutoff);
        }
    }
}
