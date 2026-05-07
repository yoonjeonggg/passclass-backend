package app_programming_development.Class.dto.chapter.response;

import app_programming_development.Class.chapter.entity.LectureChapters;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChapterWatchResponse {
    private Long id;
    private String title;
    private String videoUrl;
    private Integer chapterOrder;
    private boolean completed;

    public static ChapterWatchResponse of(LectureChapters chapter, boolean completed) {
        return ChapterWatchResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .videoUrl(chapter.getVideoUrl())
                .chapterOrder(chapter.getChapterOrder())
                .completed(completed)
                .build();
    }
}
