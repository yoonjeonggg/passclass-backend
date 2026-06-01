package app_programming_development.Class.dto.problem.response;

import app_programming_development.Class.problem.entity.Problems;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemDetailResponse {
    private Long id;
    private String content;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctAnswer;
    private String explanation;

    public static ProblemDetailResponse from(Problems problem) {
        return ProblemDetailResponse.builder()
                .id(problem.getId())
                .content(problem.getContent())
                .option1(problem.getOption1())
                .option2(problem.getOption2())
                .option3(problem.getOption3())
                .option4(problem.getOption4())
                .correctAnswer(problem.getCorrectAnswer())
                .explanation(problem.getExplanation())
                .build();
    }
}
