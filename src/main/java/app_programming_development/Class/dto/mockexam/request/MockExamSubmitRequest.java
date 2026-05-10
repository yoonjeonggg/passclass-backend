package app_programming_development.Class.dto.mockexam.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MockExamSubmitRequest {
    private List<MockExamAnswerRequest> answers;
}
