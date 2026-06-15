package app_programming_development.Class.scheduler;

import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.problem.repository.ProblemSolvesRepository;
import app_programming_development.Class.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsScheduler {

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProblemSolvesRepository problemSolvesRepository;

    // 매일 새벽 2시 30분 운영 통계 로깅 (배치성 집계)
    @Scheduled(cron = "${scheduler.statistics.cron}")
    @Transactional(readOnly = true)
    public void logDailyStatistics() {
        long totalUsers = userRepository.count();
        long totalLectures = lectureRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long totalProblemSolves = problemSolvesRepository.count();

        log.info("[Statistics] 일일 현황 - 총 사용자: {}, 총 강의: {}, 총 수강 등록: {}, 총 문제 풀이: {}",
                totalUsers, totalLectures, totalEnrollments, totalProblemSolves);
    }
}
