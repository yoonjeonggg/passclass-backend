package app_programming_development.Class.problem.repository;

import app_programming_development.Class.problem.entity.ProblemSolves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemSolvesRepository extends JpaRepository<ProblemSolves, Long> {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProblemSolves p WHERE p.user.id = :userId AND p.problems.id = :problemId")
    void deleteByUser_IdAndProblems_Id(@Param("userId") Long userId, @Param("problemId") Long problemId);

    long countByUser_Id(Long userId);
    long countByUser_IdAndIsCorrect(Long userId, boolean isCorrect);

    List<ProblemSolves> findByUser_Id(Long userId);

    @Query("SELECT p.problems.certificates.id, COUNT(p), SUM(CASE WHEN p.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM ProblemSolves p WHERE p.user.id = :userId GROUP BY p.problems.certificates.id")
    List<Object[]> findStatsByCertificateForUser(@Param("userId") Long userId);
}
