package app_programming_development.Class.mockexam.repository;

import app_programming_development.Class.mockexam.entity.MockExamResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MockExamResultsRepository extends JpaRepository<MockExamResults, Long> {
    List<MockExamResults> findByUser_IdAndMockExams_Id(Long userId, Long mockExamId);

    @Modifying
    @Query("DELETE FROM MockExamResults r WHERE r.user.id = :userId AND r.mockExams.id = :mockExamId")
    void deleteByUser_IdAndMockExams_Id(@Param("userId") Long userId, @Param("mockExamId") Long mockExamId);
}
