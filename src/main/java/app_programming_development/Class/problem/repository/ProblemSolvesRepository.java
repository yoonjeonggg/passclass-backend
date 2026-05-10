package app_programming_development.Class.problem.repository;

import app_programming_development.Class.problem.entity.ProblemSolves;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemSolvesRepository extends JpaRepository<ProblemSolves, Long> {
}
