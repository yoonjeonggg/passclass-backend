package app_programming_development.Class.mockexam.controller;

import app_programming_development.Class.dto.mockexam.request.MockExamAddQuestionRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamCreateRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamSubmitRequest;
import app_programming_development.Class.dto.mockexam.response.MockExamCreateResponse;
import app_programming_development.Class.dto.mockexam.response.MockExamListResponse;
import app_programming_development.Class.dto.mockexam.response.MockExamQuestionsResponse;
import app_programming_development.Class.dto.mockexam.response.MockExamSubmitResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.mockexam.service.MockExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock-exams")
@RequiredArgsConstructor
@Tag(name = "MockExam", description = "모의고사 관련 API")
public class MockExamController {

    private final MockExamService mockExamService;

    @PostMapping
    @Operation(summary = "모의고사 등록")
    public ResponseEntity<ApiResponse<MockExamCreateResponse>> createMockExam(@RequestBody MockExamCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mockExamService.createMockExam(request), "모의고사가 등록되었습니다."));
    }

    @GetMapping
    @Operation(summary = "모의고사 목록 조회")
    public ResponseEntity<ApiResponse<List<MockExamListResponse>>> getMockExams(@RequestParam Long certificateId) {
        return ResponseEntity.ok(ApiResponse.ok(mockExamService.getMockExams(certificateId), "조회되었습니다."));
    }

    @GetMapping("/{mockExamId}")
    @Operation(summary = "모의고사 응시 (문제 목록 조회)")
    public ResponseEntity<ApiResponse<MockExamQuestionsResponse>> getMockExamQuestions(@PathVariable Long mockExamId) {
        return ResponseEntity.ok(ApiResponse.ok(mockExamService.getMockExamQuestions(mockExamId), "조회되었습니다."));
    }

    @PostMapping("/{mockExamId}/submit")
    @Operation(summary = "모의고사 제출")
    public ResponseEntity<ApiResponse<MockExamSubmitResponse>> submitMockExam(
            @PathVariable Long mockExamId,
            @RequestBody MockExamSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mockExamService.submitMockExam(mockExamId, request), "제출되었습니다."));
    }

    @GetMapping("/{mockExamId}/results")
    @Operation(summary = "모의고사 결과 조회")
    public ResponseEntity<ApiResponse<MockExamSubmitResponse>> getMockExamResults(@PathVariable Long mockExamId) {
        return ResponseEntity.ok(ApiResponse.ok(mockExamService.getMockExamResults(mockExamId), "조회되었습니다."));
    }

    @DeleteMapping("/{mockExamId}")
    @Operation(summary = "모의고사 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteMockExam(@PathVariable Long mockExamId) {
        mockExamService.deleteMockExam(mockExamId);
        return ResponseEntity.ok(ApiResponse.ok("모의고사가 삭제되었습니다."));
    }

    @PostMapping("/{mockExamId}/questions")
    @Operation(summary = "모의고사 문제 추가")
    public ResponseEntity<ApiResponse<Void>> addQuestion(
            @PathVariable Long mockExamId,
            @RequestBody MockExamAddQuestionRequest request) {
        mockExamService.addQuestion(mockExamId, request);
        return ResponseEntity.ok(ApiResponse.ok("문제가 추가되었습니다."));
    }
}
