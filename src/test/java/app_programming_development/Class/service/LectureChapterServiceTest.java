package app_programming_development.Class.service;

import app_programming_development.Class.dto.chapter.request.LectureChapterRequest;
import app_programming_development.Class.dto.chapter.response.LectureChapterResponse;
import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.enrollment.entity.Enrollments;
import app_programming_development.Class.chapter.entity.LectureChapters;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.ChapterNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.chapter.service.LectureChapterService;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LectureChapterServiceTest {

    @Mock private LectureChapterRepository lectureChapterRepository;
    @Mock private LectureRepository lectureRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private NotificationService notificationService;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private LectureChapterService lectureChapterService;

    private Users teacher;
    private Users student;
    private Lectures lecture;
    private LectureChapters chapter;
    private LectureChapterRequest request;

    @BeforeEach
    void setUp() {
        teacher = Users.builder()
                .email("teacher@test.com")
                .password("password")
                .nickname("강사")
                .role(UserRole.TEACHER)
                .build();

        student = Users.builder()
                .email("student@test.com")
                .password("password")
                .nickname("학생")
                .role(UserRole.USER)
                .build();

        Certificates cert = Certificates.builder().name("정보처리기사").description("설명").build();
        lecture = Lectures.builder()
                .instructor(teacher)
                .certificates(cert)
                .title("스프링 강의")
                .description("설명")
                .category("백엔드")
                .build();

        chapter = LectureChapters.builder()
                .lectures(lecture)
                .title("1강 - 소개")
                .videoUrl("http://video.com/1")
                .chapterOrder(1)
                .build();

        request = new LectureChapterRequest(1L, "1강 - 소개", "http://video.com/1", 1);
    }

    @Test
    @DisplayName("챕터 등록 - 성공")
    void createChapter_성공() {
        Users enrolledUser = Users.builder()
                .email("enrolled@test.com").password("password").nickname("수강생").role(UserRole.USER).build();
        Enrollments enrollment = Enrollments.builder().user(enrolledUser).lectures(lecture).build();

        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));
        given(lectureChapterRepository.save(any())).willReturn(chapter);
        given(enrollmentRepository.findByLectures_Id(1L)).willReturn(List.of(enrollment));

        LectureChapterResponse result = lectureChapterService.createChapter(request);

        assertThat(result.getTitle()).isEqualTo("1강 - 소개");
        then(lectureChapterRepository).should().save(any());
        then(notificationService).should().createNotification(eq(enrolledUser), any(), any());
    }

    @Test
    @DisplayName("챕터 등록 - 강사가 아닌 경우 예외")
    void createChapter_강사아님_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        assertThatThrownBy(() -> lectureChapterService.createChapter(request))
                .isInstanceOf(TeacherRoleRequiredException.class);
    }

    @Test
    @DisplayName("챕터 수정 - 성공")
    void updateChapter_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(lectureChapterRepository.findById(1L)).willReturn(Optional.of(chapter));

        LectureChapterResponse result = lectureChapterService.updateChapter(1L, request);

        assertThat(result.getTitle()).isEqualTo("1강 - 소개");
    }

    @Test
    @DisplayName("챕터 수정 - 강사가 아닌 경우 예외")
    void updateChapter_강사아님_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        assertThatThrownBy(() -> lectureChapterService.updateChapter(1L, request))
                .isInstanceOf(TeacherRoleRequiredException.class);
    }

    @Test
    @DisplayName("챕터 삭제 - 성공")
    void deleteChapter_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(lectureChapterRepository.findById(1L)).willReturn(Optional.of(chapter));

        assertThatNoException().isThrownBy(() -> lectureChapterService.deleteChapter(1L));
        then(lectureChapterRepository).should().delete(chapter);
    }

    @Test
    @DisplayName("챕터 삭제 - 존재하지 않는 챕터 예외")
    void deleteChapter_챕터없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(lectureChapterRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> lectureChapterService.deleteChapter(999L))
                .isInstanceOf(ChapterNotFoundException.class);
    }

    @Test
    @DisplayName("챕터 목록 조회 - 성공")
    void getChapters_성공() {
        given(lectureRepository.existsById(1L)).willReturn(true);
        given(lectureChapterRepository.findByLectures_IdOrderByChapterOrderAsc(1L))
                .willReturn(List.of(chapter));

        List<LectureChapterResponse> result = lectureChapterService.getChapters(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("1강 - 소개");
    }

    @Test
    @DisplayName("챕터 목록 조회 - 존재하지 않는 강의 예외")
    void getChapters_강의없음_예외() {
        given(lectureRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> lectureChapterService.getChapters(999L))
                .isInstanceOf(LectureNotFoundException.class);
    }
}
