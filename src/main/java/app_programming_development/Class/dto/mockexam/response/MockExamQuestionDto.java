package app_programming_development.Class.dto.mockexam.response;

import app_programming_development.Class.problem.entity.Problems;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MockExamQuestionDto {
    private Long problemId;
    private String content;
    private String option1;
    private String option2;
    private String option3;
    private String option4;

    public static MockExamQuestionDto from(Problems problem) {
        return MockExamQuestionDto.builder()
                .problemId(problem.getId())
                .content(problem.getContent())
                .option1(problem.getOption1())
                .option2(problem.getOption2())
                .option3(problem.getOption3())
                .option4(problem.getOption4())
                .build();
    }
}
