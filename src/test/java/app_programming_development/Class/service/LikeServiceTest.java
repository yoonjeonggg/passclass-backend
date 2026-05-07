package app_programming_development.Class.service;

import app_programming_development.Class.dto.like.response.LikeResponse;
import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.like.entity.LectureLikes;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.like.repository.LectureLikeRepository;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.like.service.LikeService;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock private LectureLikeRepository lectureLikeRepository;
    @Mock private LectureRepository lectureRepository;
    @Mock private NotificationService notificationService;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private LikeService likeService;

    private Users user;
    private Lectures lecture;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .email("user@test.com")
                .password("password")
                .nickname("유저")
                .role(UserRole.USER)
                .build();

        Certificates cert = Certificates.builder().name("정보처리기사").description("설명").build();
        lecture = Lectures.builder()
                .instructor(user)
                .certificates(cert)
                .title("스프링 강의")
                .description("설명")
                .category("백엔드")
                .build();
    }

    @Test
    @DisplayName("강의 찜 등록 - 아직 찜하지 않은 경우 찜 등록 및 알림 생성")
    void toggleLike_찜등록() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));
        given(lectureLikeRepository.existsByUser_IdAndLectures_Id(any(), eq(1L))).willReturn(false);

        LikeResponse result = likeService.toggleLike(1L);

        assertThat(result.isLiked()).isTrue();
        then(lectureLikeRepository).should().save(any(LectureLikes.class));
        then(notificationService).should().createNotification(eq(user), any(), any());
    }

    @Test
    @DisplayName("강의 찜 취소 - 이미 찜한 경우 찜 취소 (알림 생성 없음)")
    void toggleLike_찜취소() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));
        given(lectureLikeRepository.existsByUser_IdAndLectures_Id(any(), eq(1L))).willReturn(true);

        LikeResponse result = likeService.toggleLike(1L);

        assertThat(result.isLiked()).isFalse();
        then(lectureLikeRepository).should().deleteByUser_IdAndLectures_Id(any(), eq(1L));
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("강의 찜 - 존재하지 않는 강의 예외")
    void toggleLike_강의없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(lectureRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleLike(999L))
                .isInstanceOf(LectureNotFoundException.class);
    }
}
