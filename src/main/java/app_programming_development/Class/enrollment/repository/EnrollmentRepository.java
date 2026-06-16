package app_programming_development.Class.enrollment.repository;

import app_programming_development.Class.enrollment.entity.Enrollments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollments, Long> {
    Long countByLectures_Id(Long lectureId);

    @Query("SELECT e.lectures.id, COUNT(e) FROM Enrollments e WHERE e.lectures.id IN :lectureIds GROUP BY e.lectures.id")
    List<Object[]> countsByLectureIds(@Param("lectureIds") List<Long> lectureIds);
    boolean existsByUserIdAndLecturesId(Long userId, Long lectureId);
    Optional<Enrollments> findByUserIdAndLecturesId(Long userId, Long lectureId);
    List<Enrollments> findByUserId(Long userId);
    Page<Enrollments> findByUserId(Long userId, Pageable pageable);
    List<Enrollments> findByLectures_Id(Long lectureId);
    List<Enrollments> findByUserIdAndIsCompleted(Long userId, boolean isCompleted);
    Page<Enrollments> findByUserIdAndIsCompleted(Long userId, boolean isCompleted, Pageable pageable);
}
