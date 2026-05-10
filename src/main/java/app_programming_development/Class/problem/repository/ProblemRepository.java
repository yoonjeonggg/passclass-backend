package app_programming_development.Class.problem.repository;

import app_programming_development.Class.problem.entity.Problems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problems, Long> {
    List<Problems> findByCertificates_IdOrderByCreatedAtDesc(Long certificateId);
}
