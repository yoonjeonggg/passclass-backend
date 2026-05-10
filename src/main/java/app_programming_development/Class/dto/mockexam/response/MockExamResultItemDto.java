package app_programming_development.Class.dto.mockexam.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MockExamResultItemDto {
    private Long problemId;
    private boolean correct;
}
