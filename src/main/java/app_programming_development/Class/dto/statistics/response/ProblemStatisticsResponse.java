package app_programming_development.Class.dto.statistics.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProblemStatisticsResponse {
    private long totalSolved;
    private long totalCorrect;
    private double correctRate;
    private List<CertificateStatDto> byCategory;

    @Getter
    @Builder
    public static class CertificateStatDto {
        private Long certificateId;
        private String certificateName;
        private long solved;
        private long correct;
        private double correctRate;
    }
}
