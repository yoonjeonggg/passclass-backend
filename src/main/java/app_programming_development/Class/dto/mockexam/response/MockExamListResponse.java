package app_programming_development.Class.dto.mockexam.response;

import app_programming_development.Class.mockexam.entity.MockExams;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MockExamListResponse {
    private Long id;
    private String title;

    public static MockExamListResponse from(MockExams exam) {
        return MockExamListResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .build();
    }
}
