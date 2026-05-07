package app_programming_development.Class.service;

import app_programming_development.Class.dto.review.request.ReviewRequest;
import app_programming_development.Class.dto.review.response.ReviewResponse;
import app_programming_development.Class.dto.review.response.ReviewSummaryResponse;
import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.review.entity.Reviews;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.conflict.DuplicateReviewException;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.forbidden.NotReviewOwnerException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.ReviewNotFoundException;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.review.repository.ReviewRepository;
import app_programming_development.Class.review.service.ReviewService;
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
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private LectureRepository lectureRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private ReviewService reviewService;

    private Users student;
    private Lectures lecture;
    private Reviews review;
    private ReviewRequest request;

    @BeforeEach
    void setUp() {
        student = Users.builder()
                .email("student@test.com")
                .password("password")
                .nickname("학생")
                .role(UserRole.USER)
                .build();
        student.setId(1L);

        Users teacher = Users.builder()
                .email("teacher@test.com")
                .password("password")
                .nickname("강사")
                .role(UserRole.TEACHER)
                .build();
        teacher.setId(2L);

        Certificates cert = Certificates.builder().name("정보처리기사").description("설명").build();
        lecture = Lectures.builder()
                .instructor(teacher)
                .certificates(cert)
                .title("스프링 강의")
                .description("설명")
                .category("백엔드")
                .build();

        review = Reviews.builder()
                .lectures(lecture)
                .user(student)
                .rating(4.5)
                .content("좋은 강의입니다.")
                .build();

        request = new ReviewRequest();
    }

    @Test
    @DisplayName("리뷰 등록 - 성공")
    void createReview_성공() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(lectureRepository.findById(any())).willReturn(Optional.of(lecture));
        given(enrollmentRepository.existsByUserIdAndLecturesId(any(), any())).willReturn(true);
        given(reviewRepository.existsByUser_IdAndLectures_Id(any(), any())).willReturn(false);

        assertThatNoException().isThrownBy(() -> reviewService.createReview(request));
        then(reviewRepository).should().save(any());
    }

    @Test
    @DisplayName("리뷰 등록 - 수강하지 않은 강의 예외")
    void createReview_미수강_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(lectureRepository.findById(any())).willReturn(Optional.of(lecture));
        given(enrollmentRepository.existsByUserIdAndLecturesId(any(), any())).willReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(NotEnrolledException.class);
    }

    @Test
    @DisplayName("리뷰 등록 - 중복 리뷰 예외")
    void createReview_중복_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(lectureRepository.findById(any())).willReturn(Optional.of(lecture));
        given(enrollmentRepository.existsByUserIdAndLecturesId(any(), any())).willReturn(true);
        given(reviewRepository.existsByUser_IdAndLectures_Id(any(), any())).willReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(DuplicateReviewException.class);
    }

    @Test
    @DisplayName("리뷰 수정 - 성공")
    void updateReview_성공() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        assertThatNoException().isThrownBy(() -> reviewService.updateReview(1L, request));
    }

    @Test
    @DisplayName("리뷰 수정 - 존재하지 않는 리뷰 예외")
    void updateReview_리뷰없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(reviewRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(999L, request))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    @DisplayName("리뷰 수정 - 본인이 아닌 경우 예외")
    void updateReview_소유자아님_예외() {
        Users otherUser = Users.builder()
                .email("other@test.com")
                .password("password")
                .nickname("다른유저")
                .role(UserRole.USER)
                .build();
        otherUser.setId(99L);
        given(securityUtils.getCurrentUser()).willReturn(otherUser);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.updateReview(1L, request))
                .isInstanceOf(NotReviewOwnerException.class);
    }

    @Test
    @DisplayName("리뷰 요약 조회 - 성공")
    void getReviewSummary_성공() {
        given(lectureRepository.existsById(1L)).willReturn(true);
        given(reviewRepository.getAverageRating(1L)).willReturn(4.2);
        given(reviewRepository.countByLectures_Id(1L)).willReturn(5L);

        ReviewSummaryResponse result = reviewService.getReviewSummary(1L);

        assertThat(result.getAverageRating()).isEqualTo(4.2);
        assertThat(result.getReviewCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("리뷰 요약 조회 - 리뷰 없을 때 0.0 반환")
    void getReviewSummary_리뷰없음() {
        given(lectureRepository.existsById(1L)).willReturn(true);
        given(reviewRepository.getAverageRating(1L)).willReturn(null);
        given(reviewRepository.countByLectures_Id(1L)).willReturn(0L);

        ReviewSummaryResponse result = reviewService.getReviewSummary(1L);

        assertThat(result.getAverageRating()).isEqualTo(0.0);
        assertThat(result.getReviewCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("리뷰 목록 조회 - 성공")
    void getReviews_성공() {
        given(lectureRepository.existsById(1L)).willReturn(true);
        given(reviewRepository.findByLectures_IdOrderByCreatedAtDesc(1L)).willReturn(List.of(review));

        List<ReviewResponse> result = reviewService.getReviews(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("학생");
    }

    @Test
    @DisplayName("리뷰 목록 조회 - 존재하지 않는 강의 예외")
    void getReviews_강의없음_예외() {
        given(lectureRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> reviewService.getReviews(999L))
                .isInstanceOf(LectureNotFoundException.class);
    }
}
