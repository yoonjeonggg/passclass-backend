package app_programming_development.Class.service;

import app_programming_development.Class.dto.notification.response.NotificationResponse;
import app_programming_development.Class.dto.notification.response.UnreadCountResponse;
import app_programming_development.Class.notification.entity.Notifications;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.NotNotificationOwnerException;
import app_programming_development.Class.exceptions.notFound.NotificationNotFoundException;
import app_programming_development.Class.notification.repository.NotificationRepository;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private NotificationService notificationService;

    private Users user;
    private Users otherUser;
    private Notifications notification;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .email("user@test.com")
                .password("password")
                .nickname("유저")
                .role(UserRole.USER)
                .build();
        user.setId(1L);

        otherUser = Users.builder()
                .email("other@test.com")
                .password("password")
                .nickname("다른유저")
                .role(UserRole.USER)
                .build();
        otherUser.setId(2L);

        notification = Notifications.builder()
                .user(user)
                .type(NotificationType.LECTURE_NEW_CHAPTER)
                .content("새 챕터가 추가되었습니다.")
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공")
    void getNotifications_성공() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(notificationRepository.findByUser_IdOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(notification)));

        Page<NotificationResponse> result = notificationService.getNotifications(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("새 챕터가 추가되었습니다.");
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void markAsRead_성공() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        assertThatNoException().isThrownBy(() -> notificationService.markAsRead(1L));
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 처리 - 존재하지 않는 알림 예외")
    void markAsRead_알림없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 본인이 아닌 경우 예외")
    void markAsRead_소유자아님_예외() {
        given(securityUtils.getCurrentUser()).willReturn(otherUser);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L))
                .isInstanceOf(NotNotificationOwnerException.class);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 - 성공")
    void getUnreadCount_성공() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(notificationRepository.countByUser_IdAndIsRead(any(), eq(false))).willReturn(3L);

        UnreadCountResponse result = notificationService.getUnreadCount();

        assertThat(result.getUnreadCount()).isEqualTo(3L);
    }
}
