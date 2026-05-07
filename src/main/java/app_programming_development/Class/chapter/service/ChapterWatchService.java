package app_programming_development.Class.chapter.service;

import app_programming_development.Class.chapter.entity.ChapterProgress;
import app_programming_development.Class.chapter.entity.LectureChapters;
import app_programming_development.Class.chapter.repository.ChapterProgressRepository;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.dto.chapter.response.ChapterWatchResponse;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.notFound.ChapterNotFoundException;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChapterWatchService {

    private final LectureChapterRepository lectureChapterRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ChapterProgressRepository chapterProgressRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ChapterWatchResponse watchChapter(Long chapterId) {
        Users currentUser = securityUtils.getCurrentUser();

        LectureChapters chapter = lectureChapterRepository.findById(chapterId)
                .orElseThrow(ChapterNotFoundException::new);

        Long lectureId = chapter.getLectures().getId();

        if (!enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lectureId)) {
            throw new NotEnrolledException();
        }

        ChapterProgress progress = chapterProgressRepository
                .findByUserIdAndChapterId(currentUser.getId(), chapterId)
                .orElseGet(() -> chapterProgressRepository.save(
                        ChapterProgress.builder()
                                .user(currentUser)
                                .chapter(chapter)
                                .build()
                ));

        return ChapterWatchResponse.of(chapter, progress.isCompleted());
    }

    @Transactional
    public void completeChapter(Long chapterId) {
        Users currentUser = securityUtils.getCurrentUser();

        LectureChapters chapter = lectureChapterRepository.findById(chapterId)
                .orElseThrow(ChapterNotFoundException::new);

        if (!enrollmentRepository.existsByUserIdAndLecturesId(
                currentUser.getId(), chapter.getLectures().getId())) {
            throw new NotEnrolledException();
        }

        ChapterProgress progress = chapterProgressRepository
                .findByUserIdAndChapterId(currentUser.getId(), chapterId)
                .orElseGet(() -> chapterProgressRepository.save(
                        ChapterProgress.builder()
                                .user(currentUser)
                                .chapter(chapter)
                                .build()
                ));

        progress.markCompleted();
    }
}
