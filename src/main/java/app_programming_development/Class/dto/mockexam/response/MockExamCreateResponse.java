package app_programming_development.Class.dto.mockexam.response;

import app_programming_development.Class.mockexam.entity.MockExams;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MockExamCreateResponse {
    private Long id;

    public static MockExamCreateResponse from(MockExams exam) {
        return MockExamCreateResponse.builder()
                .id(exam.getId())
                .build();
    }
}
