package app_programming_development.Class.problem.controller;

import app_programming_development.Class.dto.problem.request.ProblemCreateRequest;
import app_programming_development.Class.dto.problem.request.ProblemSolveRequest;
import app_programming_development.Class.dto.problem.request.ProblemUpdateRequest;
import app_programming_development.Class.dto.problem.response.ProblemCreateResponse;
import app_programming_development.Class.dto.problem.response.ProblemListResponse;
import app_programming_development.Class.dto.problem.response.ProblemSolveResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.problem.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Tag(name = "Problem", description = "문제 관련 API")
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping
    @Operation(summary = "문제 등록")
    public ResponseEntity<ApiResponse<ProblemCreateResponse>> createProblem(@RequestBody ProblemCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.createProblem(request), "문제가 등록되었습니다."));
    }

    @GetMapping
    @Operation(summary = "문제 목록 조회")
    public ResponseEntity<ApiResponse<List<ProblemListResponse>>> getProblems(@RequestParam Long certificateId) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getProblems(certificateId), "조회되었습니다."));
    }

    @PutMapping("/{problemId}")
    @Operation(summary = "문제 수정")
    public ResponseEntity<ApiResponse<ProblemCreateResponse>> updateProblem(
            @PathVariable Long problemId,
            @RequestBody ProblemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.updateProblem(problemId, request), "문제가 수정되었습니다."));
    }

    @DeleteMapping("/{problemId}")
    @Operation(summary = "문제 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long problemId) {
        problemService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.ok("문제가 삭제되었습니다."));
    }

    @PostMapping("/{problemId}/solve")
    @Operation(summary = "문제 풀이 제출")
    public ResponseEntity<ApiResponse<ProblemSolveResponse>> solveProblem(
            @PathVariable Long problemId,
            @RequestBody ProblemSolveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.solveProblem(problemId, request), "풀이가 제출되었습니다."));
    }
}
