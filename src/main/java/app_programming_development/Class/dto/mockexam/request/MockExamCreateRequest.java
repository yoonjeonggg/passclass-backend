package app_programming_development.Class.dto.mockexam.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MockExamCreateRequest {
    private Long certificateId;
    private String title;
}
