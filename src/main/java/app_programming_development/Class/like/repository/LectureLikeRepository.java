package app_programming_development.Class.like.repository;

import app_programming_development.Class.like.entity.LectureLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureLikeRepository extends JpaRepository<LectureLikes, Long> {
    Long countByLectures_Id(Long lectureId);

    @Query("SELECT l.lectures.id, COUNT(l) FROM LectureLikes l WHERE l.lectures.id IN :lectureIds GROUP BY l.lectures.id")
    List<Object[]> countsByLectureIds(@Param("lectureIds") List<Long> lectureIds);
    boolean existsByUser_IdAndLectures_Id(Long userId, Long lectureId);
    void deleteByUser_IdAndLectures_Id(Long userId, Long lectureId);
}
