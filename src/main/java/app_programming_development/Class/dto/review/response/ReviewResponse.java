package app_programming_development.Class.dto.review.response;

import app_programming_development.Class.review.entity.Reviews;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {
    private Long reviewId;
    private Double rating;
    private String content;
    private String nickname;
    private String profileUrl;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Reviews review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .nickname(review.getUser().getNickname())
                .profileUrl(review.getUser().getProfileUrl())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
