package app_programming_development.Class.chapter.repository;

import app_programming_development.Class.chapter.entity.LectureChapters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureChapterRepository extends JpaRepository<LectureChapters, Long> {
    List<LectureChapters> findByLectures_Id(Long lectureId);
    List<LectureChapters> findByLectures_IdOrderByChapterOrderAsc(Long lectureId);
}
