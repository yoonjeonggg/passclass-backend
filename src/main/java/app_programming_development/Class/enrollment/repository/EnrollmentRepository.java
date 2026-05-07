package app_programming_development.Class.enrollment.repository;

import app_programming_development.Class.enrollment.entity.Enrollments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollments, Long> {
    Long countByLectures_Id(Long lectureId);
    boolean existsByUserIdAndLecturesId(Long userId, Long lectureId);
    Optional<Enrollments> findByUserIdAndLecturesId(Long userId, Long lectureId);
    List<Enrollments> findByUserId(Long userId);
    List<Enrollments> findByLectures_Id(Long lectureId);
}
