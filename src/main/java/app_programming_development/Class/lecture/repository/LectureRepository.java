package app_programming_development.Class.lecture.repository;

import app_programming_development.Class.lecture.entity.Lectures;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lectures, Long> {
    Page<Lectures> findByCategory(String category, Pageable pageable);
    List<Lectures> findByInstructor_IdOrderByCreatedAtDesc(Long instructorId);

    @Query("SELECT l FROM Lectures l ORDER BY (SELECT COUNT(e) FROM Enrollments e WHERE e.lectures.id = l.id) DESC")
    Page<Lectures> findAllOrderByEnrollmentCountDesc(Pageable pageable);

    @Query("SELECT l FROM Lectures l WHERE l.category = :category ORDER BY (SELECT COUNT(e) FROM Enrollments e WHERE e.lectures.id = l.id) DESC")
    Page<Lectures> findByCategoryOrderByEnrollmentCountDesc(@Param("category") String category, Pageable pageable);

    @Query("SELECT l FROM Lectures l WHERE (l.title LIKE %:keyword% OR l.instructor.nickname LIKE %:keyword%)")
    Page<Lectures> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT l FROM Lectures l WHERE l.category = :category AND (l.title LIKE %:keyword% OR l.instructor.nickname LIKE %:keyword%)")
    Page<Lectures> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);
}
