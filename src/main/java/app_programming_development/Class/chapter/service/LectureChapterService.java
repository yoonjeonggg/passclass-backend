package app_programming_development.Class.chapter.service;

import app_programming_development.Class.chapter.entity.LectureChapters;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.dto.chapter.request.LectureChapterRequest;
import app_programming_development.Class.dto.chapter.response.LectureChapterResponse;
import app_programming_development.Class.enrollment.entity.Enrollments;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.ChapterNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureChapterService {

    private final LectureChapterRepository lectureChapterRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @Transactional
    public LectureChapterResponse createChapter(LectureChapterRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.TEACHER)) {
            throw new TeacherRoleRequiredException();
        }

        Lectures lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(LectureNotFoundException::new);

        LectureChapters chapter = LectureChapters.builder()
                .lectures(lecture)
                .title(request.getTitle())
                .videoUrl(request.getVideoUrl())
                .chapterOrder(request.getChapterOrder())
                .build();

        lectureChapterRepository.save(chapter);

        List<Enrollments> enrollments = enrollmentRepository.findByLectures_Id(request.getLectureId());
        for (Enrollments enrollment : enrollments) {
            notificationService.createNotification(
                    enrollment.getUser(),
                    NotificationType.LECTURE_NEW_CHAPTER,
                    lecture.getTitle() + " 강의에 새 챕터가 추가되었습니다: " + request.getTitle()
            );
        }

        return LectureChapterResponse.from(chapter);
    }

    @Transactional
    public LectureChapterResponse updateChapter(Long chapterId, LectureChapterRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.TEACHER)) {
            throw new TeacherRoleRequiredException();
        }

        LectureChapters chapter = lectureChapterRepository.findById(chapterId)
                .orElseThrow(ChapterNotFoundException::new);

        chapter.setTitle(request.getTitle());
        chapter.setVideoUrl(request.getVideoUrl());
        chapter.setChapterOrder(request.getChapterOrder());

        return LectureChapterResponse.from(chapter);
    }

    @Transactional
    public void deleteChapter(Long chapterId) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.TEACHER)) {
            throw new TeacherRoleRequiredException();
        }

        LectureChapters chapter = lectureChapterRepository.findById(chapterId)
                .orElseThrow(ChapterNotFoundException::new);

        lectureChapterRepository.delete(chapter);
    }

    @Transactional(readOnly = true)
    public List<LectureChapterResponse> getChapters(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new LectureNotFoundException();
        }
        return lectureChapterRepository.findByLectures_IdOrderByChapterOrderAsc(lectureId)
                .stream()
                .map(LectureChapterResponse::from)
                .toList();
    }
}
