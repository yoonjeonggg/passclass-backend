package app_programming_development.Class.problem.entity;

import app_programming_development.Class.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_solves", indexes = {
        @Index(name = "idx_problem_solves_user_id", columnList = "user_id"),
        @Index(name = "idx_problem_solves_problem_id", columnList = "problem_id"),
        @Index(name = "idx_problem_solves_user_problem", columnList = "user_id, problem_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProblemSolves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name="problem_id", nullable = false)
    private Problems problems;

    @Column(name="selected_answer")
    private int selectedAnswer;

    @Column(name="is_correct", nullable = false)
    private boolean isCorrect;

    @CreatedDate
    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
