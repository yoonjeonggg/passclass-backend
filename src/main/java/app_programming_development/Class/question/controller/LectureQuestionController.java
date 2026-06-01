package app_programming_development.Class.question.controller;

import app_programming_development.Class.dto.question.request.LectureAnswerRequest;
import app_programming_development.Class.dto.question.request.LectureQuestionRequest;
import app_programming_development.Class.dto.question.response.LectureQuestionResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.question.service.LectureQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lectures/{lectureId}/questions")
@RequiredArgsConstructor
@Tag(name = "LectureQuestion", description = "강의 Q&A API")
public class LectureQuestionController {

    private final LectureQuestionService questionService;

    @GetMapping
    @Operation(summary = "질문 목록 조회")
    public ResponseEntity<ApiResponse<List<LectureQuestionResponse>>> getQuestions(@PathVariable Long lectureId) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.getQuestions(lectureId), "조회되었습니다."));
    }

    @PostMapping
    @Operation(summary = "질문 등록")
    public ResponseEntity<ApiResponse<LectureQuestionResponse>> askQuestion(
            @PathVariable Long lectureId,
            @RequestBody LectureQuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.askQuestion(lectureId, request), "질문이 등록되었습니다."));
    }

    @PostMapping("/{questionId}/answer")
    @Operation(summary = "질문 답변 등록")
    public ResponseEntity<ApiResponse<LectureQuestionResponse>> answerQuestion(
            @PathVariable Long lectureId,
            @PathVariable Long questionId,
            @RequestBody LectureAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.answerQuestion(lectureId, questionId, request), "답변이 등록되었습니다."));
    }
}
