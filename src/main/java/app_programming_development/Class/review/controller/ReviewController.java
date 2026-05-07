package app_programming_development.Class.review.controller;

import app_programming_development.Class.dto.review.request.ReviewRequest;
import app_programming_development.Class.dto.review.response.ReviewResponse;
import app_programming_development.Class.dto.review.response.ReviewSummaryResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "리뷰 등록", description = "강의 리뷰 등록 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 중인 강의에만 리뷰를 작성할 수 있습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 강의를 찾을 수 없습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 리뷰를 작성한 강의입니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> createReview(@RequestBody ReviewRequest request) {
        reviewService.createReview(request);
        return ResponseEntity.ok(ApiResponse.ok("리뷰가 등록되었습니다."));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "리뷰 수정 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 리뷰만 수정할 수 있습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 리뷰를 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest request) {
        reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.ok("리뷰가 수정되었습니다."));
    }

    @GetMapping("/summary")
    @Operation(summary = "리뷰 조회 (요약)", description = "강의의 평균 별점과 리뷰 개수를 조회하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 강의를 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(@RequestParam Long lectureId) {
        ReviewSummaryResponse result = reviewService.getReviewSummary(lectureId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping
    @Operation(summary = "리뷰 목록 조회", description = "강의의 리뷰 목록을 조회하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 강의를 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(@RequestParam Long lectureId) {
        List<ReviewResponse> result = reviewService.getReviews(lectureId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }
}
