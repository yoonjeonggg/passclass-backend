package app_programming_development.Class.service;

import app_programming_development.Class.dto.lecture.request.LectureRequest;
import app_programming_development.Class.dto.lecture.response.LectureCreateResponse;
import app_programming_development.Class.dto.lecture.response.LectureDetailResponse;
import app_programming_development.Class.dto.lecture.response.LectureListDto;
import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.SortType;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.review.repository.ReviewRepository;
import app_programming_development.Class.like.repository.LectureLikeRepository;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.lecture.service.LectureService;
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
class LectureServiceTest {

    @Mock private LectureRepository lectureRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private LectureLikeRepository lectureLikeRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LectureChapterRepository lectureChapterRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private LectureService lectureService;

    private Users teacher;
    private Users student;
    private Certificates certificate;
    private Lectures lecture;

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

        certificate = Certificates.builder()
                .name("정보처리기사")
                .description("자격증 설명")
                .build();

        lecture = Lectures.builder()
                .instructor(teacher)
                .certificates(certificate)
                .title("스프링 강의")
                .description("스프링 기초")
                .category("백엔드")
                .thumbnailUrl("http://example.com/thumb.jpg")
                .build();
    }

    @Test
    @DisplayName("강의 목록 조회 - 별점이 실제 평균으로 계산된다")
    void getLectures_rating이_실제값으로_계산된다() {
        given(lectureRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(lecture)));
        given(reviewRepository.getAverageRating(any())).willReturn(4.5);

        Page<LectureListDto> result = lectureService.getLectures(0, 10, null, SortType.LATEST);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("강의 목록 조회 - 리뷰 없을 때 별점 0.0 반환")
    void getLectures_리뷰없을때_별점_0() {
        given(lectureRepository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(lecture)));
        given(reviewRepository.getAverageRating(any())).willReturn(null);

        Page<LectureListDto> result = lectureService.getLectures(0, 10, null, SortType.LATEST);

        assertThat(result.getContent().get(0).getRating()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("강의 목록 조회 - 카테고리 필터링")
    void getLectures_카테고리_필터링() {
        given(lectureRepository.findByCategory(eq("백엔드"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(lecture)));
        given(reviewRepository.getAverageRating(any())).willReturn(0.0);

        Page<LectureListDto> result = lectureService.getLectures(0, 10, "백엔드", SortType.LATEST);

        assertThat(result.getContent()).hasSize(1);
        then(lectureRepository).should().findByCategory(eq("백엔드"), any(Pageable.class));
    }

    @Test
    @DisplayName("강의 상세 조회 - isLiked 포함 정상 반환")
    void getLecture_isLiked_포함() {
        given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));
        given(reviewRepository.getAverageRating(1L)).willReturn(4.0);
        given(lectureLikeRepository.countByLectures_Id(1L)).willReturn(5L);
        given(enrollmentRepository.countByLectures_Id(1L)).willReturn(10L);
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(lectureLikeRepository.existsByUser_IdAndLectures_Id(any(), eq(1L))).willReturn(true);
        given(lectureChapterRepository.findByLectures_Id(1L)).willReturn(List.of());

        LectureDetailResponse result = lectureService.getLecture(1L);

        assertThat(result.getIsLiked()).isTrue();
        assertThat(result.getRating()).isEqualTo(4.0);
        assertThat(result.getLikeCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("강의 상세 조회 - 존재하지 않는 강의 예외")
    void getLecture_존재하지않는강의_예외() {
        given(lectureRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> lectureService.getLecture(999L))
                .isInstanceOf(LectureNotFoundException.class);
    }

    @Test
    @DisplayName("강의 생성 - 강사가 아닌 경우 예외")
    void createLecture_강사아님_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        LectureRequest request = new LectureRequest(1L, "제목", "설명", "백엔드", "url");

        assertThatThrownBy(() -> lectureService.createLecture(request))
                .isInstanceOf(TeacherRoleRequiredException.class);
    }

    @Test
    @DisplayName("강의 생성 - 강사 정상 생성")
    void createLecture_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(certificateRepository.findById(any())).willReturn(Optional.of(certificate));
        given(lectureRepository.save(any())).willReturn(lecture);

        LectureRequest request = new LectureRequest(1L, "스프링 강의", "설명", "백엔드", "url");

        LectureCreateResponse result = lectureService.createLecture(request);

        assertThat(result).isNotNull();
        then(lectureRepository).should().save(any());
    }
}
