package app_programming_development.Class.chapter.service;

import app_programming_development.Class.chapter.entity.ChapterProgress;
import app_programming_development.Class.chapter.entity.LectureChapters;
import app_programming_development.Class.chapter.repository.ChapterProgressRepository;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.dto.chapter.response.ChapterWatchResponse;
import app_programming_development.Class.dto.chapter.response.LectureProgressResponse;
import app_programming_development.Class.dto.chapter.response.MyChapterProgress;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.notFound.ChapterNotFoundException;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

        return ChapterWatchResponse.of(chapter, progress.isCompleted(), progress.getWatchedSeconds());
    }

    @Transactional
    public void saveProgress(Long chapterId, int watchedSeconds) {
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

        progress.updateWatchedSeconds(watchedSeconds);
        log.debug("Progress saved: userId={}, chapterId={}, watchedSeconds={}",
                currentUser.getId(), chapterId, watchedSeconds);
    }

    @Transactional(readOnly = true)
    public LectureProgressResponse getMyProgress(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();

        List<LectureChapters> allChapters = lectureChapterRepository.findByLectures_IdOrderByChapterOrderAsc(lectureId);
        List<ChapterProgress> myProgress = chapterProgressRepository
                .findByUser_IdAndChapter_Lectures_Id(currentUser.getId(), lectureId);

        int totalCount = allChapters.size();
        int completedCount = (int) myProgress.stream().filter(ChapterProgress::isCompleted).count();
        int progressPercent = totalCount > 0 ? (completedCount * 100 / totalCount) : 0;

        List<MyChapterProgress> chapters = myProgress.stream()
                .map(MyChapterProgress::of)
                .collect(Collectors.toList());

        return new LectureProgressResponse(completedCount, totalCount, progressPercent, chapters);
    }

    @Transactional
    public void completeChapter(Long chapterId) {
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

        progress.markCompleted();

        // 전체/완료 챕터 수 기반으로 수강 진도 및 완료 여부 자동 업데이트
        int totalChapters = lectureChapterRepository.countByLectures_Id(lectureId);
        int completedChapters = (int) chapterProgressRepository
                .findByUser_IdAndChapter_Lectures_Id(currentUser.getId(), lectureId)
                .stream().filter(ChapterProgress::isCompleted).count();

        enrollmentRepository.findByUserIdAndLecturesId(currentUser.getId(), lectureId)
                .ifPresent(enrollment -> enrollment.updateProgress(completedChapters, totalChapters));

        log.info("Chapter completed: userId={}, chapterId={}, progress={}/{}",
                currentUser.getId(), chapterId, completedChapters, totalChapters);
    }
}
