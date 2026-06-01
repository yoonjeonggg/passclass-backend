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
    private String reply;
    private LocalDateTime replyAt;
    private String instructorNickname;
    private String instructorProfileUrl;

    public static ReviewResponse from(Reviews review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .nickname(review.getUser().getNickname())
                .profileUrl(review.getUser().getProfileUrl())
                .createdAt(review.getCreatedAt())
                .reply(review.getReply())
                .replyAt(review.getReplyAt())
                .instructorNickname(review.getLectures().getInstructor().getNickname())
                .instructorProfileUrl(review.getLectures().getInstructor().getProfileUrl())
                .build();
    }
}
