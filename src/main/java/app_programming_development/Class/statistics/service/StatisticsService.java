package app_programming_development.Class.statistics.service;

import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.dto.statistics.response.ProblemStatisticsResponse;
import app_programming_development.Class.problem.repository.ProblemSolvesRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final ProblemSolvesRepository problemSolvesRepository;
    private final CertificateRepository certificateRepository;
    private final SecurityUtils securityUtils;

    public ProblemStatisticsResponse getProblemStatistics() {
        Users user = securityUtils.getCurrentUser();
        long totalSolved = problemSolvesRepository.countByUser_Id(user.getId());
        long totalCorrect = problemSolvesRepository.countByUser_IdAndIsCorrect(user.getId(), true);
        double correctRate = totalSolved > 0 ? (double) totalCorrect / totalSolved * 100 : 0.0;

        List<Object[]> rawStats = problemSolvesRepository.findStatsByCertificateForUser(user.getId());
        List<ProblemStatisticsResponse.CertificateStatDto> byCategory = rawStats.stream()
                .map(row -> {
                    Long certId = (Long) row[0];
                    long solved = ((Number) row[1]).longValue();
                    long correct = ((Number) row[2]).longValue();
                    String certName = certificateRepository.findById(certId)
                            .map(c -> c.getName())
                            .orElse("알 수 없음");
                    return ProblemStatisticsResponse.CertificateStatDto.builder()
                            .certificateId(certId)
                            .certificateName(certName)
                            .solved(solved)
                            .correct(correct)
                            .correctRate(solved > 0 ? (double) correct / solved * 100 : 0.0)
                            .build();
                })
                .toList();

        return ProblemStatisticsResponse.builder()
                .totalSolved(totalSolved)
                .totalCorrect(totalCorrect)
                .correctRate(Math.round(correctRate * 10.0) / 10.0)
                .byCategory(byCategory)
                .build();
    }
}
