package app_programming_development.Class.dto.problem.response;

import app_programming_development.Class.problem.entity.Problems;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemListResponse {
    private Long id;
    private String content;

    public static ProblemListResponse from(Problems problem) {
        return ProblemListResponse.builder()
                .id(problem.getId())
                .content(problem.getContent())
                .build();
    }
}
