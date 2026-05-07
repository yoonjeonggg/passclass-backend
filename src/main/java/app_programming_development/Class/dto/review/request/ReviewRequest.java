package app_programming_development.Class.dto.review.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {
    private Long lectureId;
    private Double rating;
    private String content;
}
