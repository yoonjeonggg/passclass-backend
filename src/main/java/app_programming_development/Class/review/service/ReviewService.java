package app_programming_development.Class.review.service;

import app_programming_development.Class.dto.review.request.ReviewRequest;
import app_programming_development.Class.dto.review.response.ReviewResponse;
import app_programming_development.Class.dto.review.response.ReviewSummaryResponse;
import app_programming_development.Class.discord.DiscordWebhookService;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.enums.ReviewSortType;
import app_programming_development.Class.exceptions.conflict.DuplicateReviewException;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.forbidden.NotReviewOwnerException;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.ReviewNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.review.entity.Reviews;
import app_programming_development.Class.review.repository.ReviewRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;
    private final DiscordWebhookService discordWebhookService;

    @Transactional
    public void createReview(ReviewRequest request) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(LectureNotFoundException::new);

        if (!enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lecture.getId())) {
            throw new NotEnrolledException();
        }
        if (reviewRepository.existsByUser_IdAndLectures_Id(currentUser.getId(), lecture.getId())) {
            throw new DuplicateReviewException();
        }

        Reviews review = Reviews.builder()
                .lectures(lecture)
                .user(currentUser)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);
        log.info("Review created: userId={}, lectureId={}, rating={}",
                currentUser.getId(), lecture.getId(), request.getRating());
        if (request.getRating() != null) {
            discordWebhookService.sendNewReview(currentUser.getNickname(), lecture.getTitle(), (int) Math.round(request.getRating()));
        }
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewRequest request) {
        Users currentUser = securityUtils.getCurrentUser();
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        if (!Objects.equals(review.getUser().getId(), currentUser.getId())) {
            throw new NotReviewOwnerException();
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());
        log.info("Review updated: reviewId={}, userId={}", reviewId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new LectureNotFoundException();
        }
        Double avgRating = reviewRepository.getAverageRating(lectureId);
        Long count = reviewRepository.countByLectures_Id(lectureId);
        return ReviewSummaryResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(count)
                .build();
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Users currentUser = securityUtils.getCurrentUser();
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        if (!Objects.equals(review.getUser().getId(), currentUser.getId())) {
            throw new NotReviewOwnerException();
        }

        reviewRepository.delete(review);
        log.info("Review deleted: reviewId={}, userId={}", reviewId, currentUser.getId());
    }

    @Transactional
    public void replyToReview(Long reviewId, String reply) {
        Users currentUser = securityUtils.getCurrentUser();
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        Long instructorId = review.getLectures().getInstructor().getId();
        if (!Objects.equals(instructorId, currentUser.getId())) {
            throw new TeacherRoleRequiredException();
        }

        review.setReply(reply);
        review.setReplyAt(LocalDateTime.now());

        notificationService.createNotification(
                review.getUser(),
                NotificationType.REVIEW_COMMENT,
                review.getLectures().getTitle() + " 강의 리뷰에 강사 답글이 등록되었습니다."
        );
        log.info("Review replied by instructor: reviewId={}, instructorId={}", reviewId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long lectureId, ReviewSortType sort) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new LectureNotFoundException();
        }
        List<Reviews> reviews = switch (sort) {
            case RATING_HIGH -> reviewRepository.findByLectures_IdOrderByRatingDescCreatedAtDesc(lectureId);
            case RATING_LOW -> reviewRepository.findByLectures_IdOrderByRatingAscCreatedAtDesc(lectureId);
            default -> reviewRepository.findByLectures_IdOrderByCreatedAtDesc(lectureId);
        };
        return reviews.stream().map(ReviewResponse::from).toList();
    }
}
