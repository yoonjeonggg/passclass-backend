package app_programming_development.Class.mockexam.entity;

import app_programming_development.Class.problem.entity.Problems;
import app_programming_development.Class.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mock_exam_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MockExamResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "mock_exam_id", nullable = false)
    private MockExams mockExams;

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problems problems;

    @Column(name = "selected_answer", nullable = false)
    private int selectedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
