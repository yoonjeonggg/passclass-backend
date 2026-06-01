package app_programming_development.Class.question.repository;

import app_programming_development.Class.question.entity.LectureQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureQuestionRepository extends JpaRepository<LectureQuestions, Long> {
    List<LectureQuestions> findByLecture_IdOrderByCreatedAtDesc(Long lectureId);
}
