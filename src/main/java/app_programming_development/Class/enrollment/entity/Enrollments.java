package app_programming_development.Class.enrollment.entity;

import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lecture_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Enrollments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lectures lectures;

    @Column(name = "progress_rate")
    private int progressRate;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Enrollments(Users user, Lectures lectures) {
        this.user = user;
        this.lectures = lectures;
        this.progressRate = 0;
        this.isCompleted = false;
    }

    public void updateProgress(int completedChapters, int totalChapters) {
        if (totalChapters == 0) return;
        this.progressRate = (completedChapters * 100) / totalChapters;
        this.isCompleted = (this.progressRate == 100);
    }
}
