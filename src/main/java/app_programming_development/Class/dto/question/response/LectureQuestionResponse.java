package app_programming_development.Class.dto.question.response;

import app_programming_development.Class.question.entity.LectureQuestions;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class LectureQuestionResponse {
    private Long id;
    private Long lectureId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String content;
    private String answer;
    private String answeredAt;
    private String answererNickname;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static LectureQuestionResponse from(LectureQuestions q) {
        return LectureQuestionResponse.builder()
                .id(q.getId())
                .lectureId(q.getLecture().getId())
                .userId(q.getUser().getId())
                .nickname(q.getUser().getNickname())
                .profileImage(q.getUser().getProfileUrl())
                .content(q.getContent())
                .answer(q.getAnswer())
                .answeredAt(q.getAnsweredAt() != null ? q.getAnsweredAt().format(FORMATTER) : null)
                .answererNickname(q.getAnswerer() != null ? q.getAnswerer().getNickname() : null)
                .createdAt(q.getCreatedAt() != null ? q.getCreatedAt().format(FORMATTER) : null)
                .build();
    }
}
