package app_programming_development.Class.lecture.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.config.CacheConfig;
import app_programming_development.Class.dto.certificate.response.CertificateInfo;
import app_programming_development.Class.dto.chapter.response.ChapterDto;
import app_programming_development.Class.dto.lecture.request.LectureRequest;
import app_programming_development.Class.dto.lecture.response.InstructorDto;
import app_programming_development.Class.dto.lecture.response.InstructorProfileResponse;
import app_programming_development.Class.dto.lecture.response.LectureCreateResponse;
import app_programming_development.Class.dto.lecture.response.LectureDetailResponse;
import app_programming_development.Class.dto.lecture.response.LectureListDto;
import app_programming_development.Class.enrollment.entity.Enrollments;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.enums.SortType;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.AdminRoleRequiredException;
import app_programming_development.Class.exceptions.forbidden.NotLectureOwnerException;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.exceptions.unauthorized.NotAuthenticatedException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.like.repository.LectureLikeRepository;
import app_programming_development.Class.discord.DiscordWebhookService;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.review.repository.ReviewRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LectureService {
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ReviewRepository reviewRepository;
    private final LectureLikeRepository lectureLikeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureChapterRepository lectureChapterRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationService notificationService;
    private final DiscordWebhookService discordWebhookService;

    @Transactional
    @CacheEvict(value = CacheConfig.LECTURES, allEntries = true)
    public LectureCreateResponse createLecture(LectureRequest request) {
        Users instructor = securityUtils.getCurrentUser();
        if (!instructor.getRole().equals(UserRole.TEACHER)) {
            throw new TeacherRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(request.getCertificateId())
                .orElseThrow(CertificateNotFoundException::new);

        Lectures lecture = Lectures.builder()
                .instructor(instructor)
                .certificates(certificate)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        lectureRepository.save(lecture);

        log.info("Lecture created: lectureId={}, instructorId={}, title={}",
                lecture.getId(), instructor.getId(), lecture.getTitle());
        discordWebhookService.sendNewLecture(lecture.getTitle(), instructor.getNickname(), lecture.getCategory());
        return LectureCreateResponse.from(lecture);
    }

    @Cacheable(value = CacheConfig.LECTURES, key = "#keyword + '_' + #category + '_' + #sort + '_' + #page + '_' + #size")
    public Page<LectureListDto> getLectures(int page, int size, String category, SortType sort, String keyword) {
        Page<Lectures> lectures;
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword) {
            Sort sorting = Sort.by(Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(page, size, sorting);
            if (category != null && !category.isEmpty()) {
                lectures = lectureRepository.searchByKeywordAndCategory(keyword, category, pageable);
            } else {
                lectures = lectureRepository.searchByKeyword(keyword, pageable);
            }
        } else if (sort == SortType.ENROLLMENT) {
            Pageable pageable = PageRequest.of(page, size);
            if (category != null && !category.isEmpty()) {
                lectures = lectureRepository.findByCategoryOrderByEnrollmentCountDesc(category, pageable);
            } else {
                lectures = lectureRepository.findAllOrderByEnrollmentCountDesc(pageable);
            }
        } else {
            Sort sorting;
            switch (sort) {
                case POPULAR:
                    sorting = Sort.by(Sort.Direction.DESC, "likeCount");
                    break;
                case OLDEST:
                    sorting = Sort.by(Sort.Direction.ASC, "createdAt");
                    break;
                case LATEST:
                default:
                    sorting = Sort.by(Sort.Direction.DESC, "createdAt");
            }
            Pageable pageable = PageRequest.of(page, size, sorting);
            if (category != null && !category.isEmpty()) {
                lectures = lectureRepository.findByCategory(category, pageable);
            } else {
                lectures = lectureRepository.findAll(pageable);
            }
        }
        return lectures.map(lecture -> {
            Double avgRating = reviewRepository.getAverageRating(lecture.getId());
            Long enrollCount = enrollmentRepository.countByLectures_Id(lecture.getId());
            Long likeCount = lectureLikeRepository.countByLectures_Id(lecture.getId());
            return LectureListDto.from(lecture, avgRating != null ? avgRating : 0.0, enrollCount, likeCount);
        });
    }

    @Transactional
    @CacheEvict(value = CacheConfig.LECTURES, allEntries = true)
    public void updateLecture(Long lectureId, LectureRequest request) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);
        boolean isOwner = currentUser.getRole().equals(UserRole.TEACHER)
                && lecture.getInstructor().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            throw new NotLectureOwnerException();
        }

        Certificates certificate = certificateRepository.findById(request.getCertificateId())
                .orElseThrow(CertificateNotFoundException::new);
        lecture.setTitle(request.getTitle());
        lecture.setDescription(request.getDescription());
        lecture.setCategory(request.getCategory());
        lecture.setThumbnailUrl(request.getThumbnailUrl());
        lecture.setCertificates(certificate);

        List<Enrollments> enrollments = enrollmentRepository.findByLectures_Id(lectureId);
        for (Enrollments enrollment : enrollments) {
            notificationService.createNotification(
                    enrollment.getUser(),
                    NotificationType.LECTURE_UPDATED,
                    lecture.getTitle() + " 강의가 업데이트되었습니다."
            );
        }
        log.info("Lecture updated: lectureId={}, updatedBy={}", lectureId, currentUser.getId());
    }

    @Transactional
    @CacheEvict(value = CacheConfig.LECTURES, allEntries = true)
    public void deleteLecture(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);
        boolean isOwner = currentUser.getRole().equals(UserRole.TEACHER)
                && lecture.getInstructor().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            throw new NotLectureOwnerException();
        }

        lectureRepository.delete(lecture);
        log.info("Lecture deleted: lectureId={}, deletedBy={}", lectureId, currentUser.getId());
    }

    public LectureDetailResponse getLecture(Long lectureId) {
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        Double rating = reviewRepository.getAverageRating(lectureId);
        Long likeCount = lectureLikeRepository.countByLectures_Id(lectureId);
        Long studentCount = enrollmentRepository.countByLectures_Id(lectureId);

        boolean isLiked = false;
        try {
            Users currentUser = securityUtils.getCurrentUser();
            isLiked = lectureLikeRepository.existsByUser_IdAndLectures_Id(currentUser.getId(), lectureId);
        } catch (NotAuthenticatedException | UserNotFoundException ignored) {}

        List<ChapterDto> chapters = lectureChapterRepository.findByLectures_Id(lectureId)
                .stream()
                .map(ch -> ChapterDto.builder()
                        .id(ch.getId())
                        .title(ch.getTitle())
                        .order(ch.getChapterOrder())
                        .build())
                .toList();

        InstructorDto instructor = InstructorDto.builder()
                .id(lecture.getInstructor().getId())
                .nickname(lecture.getInstructor().getNickname())
                .profileImage(lecture.getInstructor().getProfileUrl())
                .build();

        return LectureDetailResponse.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .category(lecture.getCategory())
                .thumbnailUrl(lecture.getThumbnailUrl())
                .description(lecture.getDescription())
                .rating(rating != null ? rating : 0.0)
                .isLiked(isLiked)
                .likeCount(likeCount)
                .studentCount(studentCount)
                .chapterCount(chapters.size())
                .instructor(instructor)
                .chapters(chapters)
                .certificate(CertificateInfo.builder()
                        .id(lecture.getCertificates().getId())
                        .name(lecture.getCertificates().getName())
                        .build())
                .build();
    }

    public InstructorProfileResponse getInstructorProfile(Long instructorId) {
        Users instructor = userRepository.findById(instructorId)
                .orElseThrow(UserNotFoundException::new);

        List<Lectures> lectures = lectureRepository.findByInstructor_IdOrderByCreatedAtDesc(instructorId);

        long totalStudents = lectures.stream()
                .mapToLong(l -> enrollmentRepository.countByLectures_Id(l.getId()))
                .sum();

        List<LectureListDto> lectureDtos = lectures.stream()
                .map(l -> {
                    Double avgRating = reviewRepository.getAverageRating(l.getId());
                    Long enrollCount = enrollmentRepository.countByLectures_Id(l.getId());
                    Long likeCount = lectureLikeRepository.countByLectures_Id(l.getId());
                    return LectureListDto.from(l, avgRating != null ? avgRating : 0.0, enrollCount, likeCount);
                })
                .collect(Collectors.toList());

        return new InstructorProfileResponse(
                instructor.getId(),
                instructor.getNickname(),
                instructor.getProfileUrl(),
                lectures.size(),
                totalStudents,
                lectureDtos
        );
    }
}
