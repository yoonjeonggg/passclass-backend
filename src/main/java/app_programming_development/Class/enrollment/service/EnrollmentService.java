package app_programming_development.Class.enrollment.service;

import app_programming_development.Class.dto.enrollment.response.EnrollmentResponse;
import app_programming_development.Class.enrollment.entity.Enrollments;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.exceptions.conflict.AlreadyEnrolledException;
import app_programming_development.Class.exceptions.notFound.EnrollmentNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.discord.DiscordWebhookService;
import app_programming_development.Class.logging.AuditLog;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureRepository lectureRepository;
    private final SecurityUtils securityUtils;
    private final DiscordWebhookService discordWebhookService;

    @AuditLog(action = "ENROLL")
    @Transactional
    public EnrollmentResponse enroll(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();

        if (enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lectureId)) {
            throw new AlreadyEnrolledException();
        }

        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        Enrollments enrollment = Enrollments.builder()
                .user(currentUser)
                .lectures(lecture)
                .build();

        enrollmentRepository.save(enrollment);
        log.info("Enrollment created: userId={}, lectureId={}", currentUser.getId(), lectureId);
        discordWebhookService.sendEnrollment(currentUser.getNickname(), lecture.getTitle());
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void cancelEnrollment(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();

        Enrollments enrollment = enrollmentRepository
                .findByUserIdAndLecturesId(currentUser.getId(), lectureId)
                .orElseThrow(EnrollmentNotFoundException::new);

        enrollmentRepository.delete(enrollment);
        log.info("Enrollment cancelled: userId={}, lectureId={}", currentUser.getId(), lectureId);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyEnrollments(int page, int size) {
        Users currentUser = securityUtils.getCurrentUser();
        return enrollmentRepository.findByUserId(currentUser.getId(), PageRequest.of(page, size))
                .map(EnrollmentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyCompletedEnrollments(int page, int size) {
        Users currentUser = securityUtils.getCurrentUser();
        return enrollmentRepository.findByUserIdAndIsCompleted(currentUser.getId(), true, PageRequest.of(page, size))
                .map(EnrollmentResponse::from);
    }
}
