package app_programming_development.Class.statistics.controller;

import app_programming_development.Class.dto.statistics.response.ProblemStatisticsResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "학습 통계 API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/problems")
    @Operation(summary = "문제 풀이 통계 조회", description = "전체 문제 풀이 수, 정답률, 분야별 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<ProblemStatisticsResponse>> getProblemStatistics() {
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getProblemStatistics(), "조회되었습니다."));
    }
}
