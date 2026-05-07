package app_programming_development.Class.dto.review.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewSummaryResponse {
    private Double averageRating;
    private Long reviewCount;
}
