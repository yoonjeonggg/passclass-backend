package app_programming_development.Class.dto.problem.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProblemSolveResponse {
    private boolean correct;
    private String explanation;
}
