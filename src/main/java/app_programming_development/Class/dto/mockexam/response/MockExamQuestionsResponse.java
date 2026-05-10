package app_programming_development.Class.dto.mockexam.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MockExamQuestionsResponse {
    private Long mockExamId;
    private String title;
    private List<MockExamQuestionDto> questions;
}
