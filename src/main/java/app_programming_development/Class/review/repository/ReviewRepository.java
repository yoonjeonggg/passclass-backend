package app_programming_development.Class.review.repository;

import app_programming_development.Class.review.entity.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.lectures.id = :lectureId")
    Double getAverageRating(@Param("lectureId") Long lectureId);

    Long countByLectures_Id(Long lectureId);
    boolean existsByUser_IdAndLectures_Id(Long userId, Long lectureId);
    List<Reviews> findByLectures_IdOrderByCreatedAtDesc(Long lectureId);
    Optional<Reviews> findByUser_IdAndLectures_Id(Long userId, Long lectureId);
}
