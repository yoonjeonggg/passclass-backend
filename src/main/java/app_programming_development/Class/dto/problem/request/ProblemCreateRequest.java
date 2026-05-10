package app_programming_development.Class.dto.problem.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProblemCreateRequest {
    private Long certificateId;
    private String content;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctAnswer;
    private String explanation;
}
