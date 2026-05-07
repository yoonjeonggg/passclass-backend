package app_programming_development.Class.enrollment.service;

import app_programming_development.Class.dto.enrollment.response.EnrollmentResponse;
import app_programming_development.Class.enrollment.entity.Enrollments;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.exceptions.conflict.AlreadyEnrolledException;
import app_programming_development.Class.exceptions.notFound.EnrollmentNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureRepository lectureRepository;
    private final SecurityUtils securityUtils;

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
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public void cancelEnrollment(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();

        Enrollments enrollment = enrollmentRepository
                .findByUserIdAndLecturesId(currentUser.getId(), lectureId)
                .orElseThrow(EnrollmentNotFoundException::new);

        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        Users currentUser = securityUtils.getCurrentUser();
        return enrollmentRepository.findByUserId(currentUser.getId())
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }
}
