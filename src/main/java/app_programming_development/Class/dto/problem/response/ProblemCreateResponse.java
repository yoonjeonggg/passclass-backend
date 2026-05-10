package app_programming_development.Class.dto.problem.response;

import app_programming_development.Class.problem.entity.Problems;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemCreateResponse {
    private Long id;

    public static ProblemCreateResponse from(Problems problem) {
        return ProblemCreateResponse.builder()
                .id(problem.getId())
                .build();
    }
}
