package app_programming_development.Class.dto.review.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {

    @NotNull(message = "강의 ID를 입력해주세요.")
    private Long lectureId;

    @NotNull(message = "별점을 입력해주세요.")
    @DecimalMin(value = "1.0", message = "별점은 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "별점은 5.0 이하이어야 합니다.")
    private Double rating;

    private String content;
}
