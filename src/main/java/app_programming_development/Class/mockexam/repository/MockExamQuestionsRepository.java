package app_programming_development.Class.mockexam.repository;

import app_programming_development.Class.mockexam.entity.MockExamQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockExamQuestionsRepository extends JpaRepository<MockExamQuestions, Long> {
    List<MockExamQuestions> findByMockExams_Id(Long mockExamId);
    boolean existsByMockExams_IdAndProblems_Id(Long mockExamId, Long problemId);
}
