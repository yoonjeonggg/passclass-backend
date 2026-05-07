package app_programming_development.Class.like.repository;

import app_programming_development.Class.like.entity.LectureLikes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureLikeRepository extends JpaRepository<LectureLikes, Long> {
    Long countByLectures_Id(Long lectureId);
    boolean existsByUser_IdAndLectures_Id(Long userId, Long lectureId);
    void deleteByUser_IdAndLectures_Id(Long userId, Long lectureId);
}
