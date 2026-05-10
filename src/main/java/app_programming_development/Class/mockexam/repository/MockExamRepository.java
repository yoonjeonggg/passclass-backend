package app_programming_development.Class.mockexam.repository;

import app_programming_development.Class.mockexam.entity.MockExams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockExamRepository extends JpaRepository<MockExams, Long> {
    List<MockExams> findByCertificates_IdOrderByCreatedAtDesc(Long certificateId);
}
