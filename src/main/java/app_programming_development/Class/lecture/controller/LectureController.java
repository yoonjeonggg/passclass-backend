package app_programming_development.Class.lecture.controller;

import app_programming_development.Class.dto.lecture.request.LectureRequest;
import app_programming_development.Class.dto.lecture.response.InstructorProfileResponse;
import app_programming_development.Class.dto.lecture.response.LectureCreateResponse;
import app_programming_development.Class.dto.lecture.response.LectureDetailResponse;
import app_programming_development.Class.dto.lecture.response.LectureListDto;
import app_programming_development.Class.enums.SortType;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
@Tag(name = "Lecture", description = "강의 관련 API")
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    @Operation(summary = "강의 생성", description = "강의 생성 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강의는 강사만 생성할 수 있습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<LectureCreateResponse>> createLecture(@RequestBody LectureRequest request) {
        LectureCreateResponse result = lectureService.createLecture(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "생성되었습니다."));
    }

    @GetMapping
    @Operation(summary = "강의 목록 조회", description = "강의 목록 조회 시 사용하는 API 입니다. keyword 파라미터로 강의 제목/강사명 검색 가능.")
    public ResponseEntity<ApiResponse<Page<LectureListDto>>> getLectures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "LATEST") SortType sort,
            @RequestParam(required = false) String keyword) {
        Page<LectureListDto> result = lectureService.getLectures(page, size, category, sort, keyword);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/{lectureId}")
    @Operation(summary = "강의 상세 조회", description = "강의 상세 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<LectureDetailResponse>> getLecture(@PathVariable Long lectureId) {
        LectureDetailResponse result = lectureService.getLecture(lectureId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "강사 프로필 조회", description = "강사 ID로 프로필과 강의 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<InstructorProfileResponse>> getInstructorProfile(@PathVariable Long instructorId) {
        InstructorProfileResponse result = lectureService.getInstructorProfile(instructorId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PutMapping("/{lectureId}")
    @Operation(summary = "강의 수정 (관리자)", description = "관리자가 강의를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자만 가능한 권한입니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> updateLecture(@PathVariable Long lectureId, @RequestBody LectureRequest request) {
        lectureService.updateLecture(lectureId, request);
        return ResponseEntity.ok(ApiResponse.ok("수정되었습니다."));
    }

    @DeleteMapping("/{lectureId}")
    @Operation(summary = "강의 삭제 (관리자)", description = "관리자가 강의를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자만 가능한 권한입니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> deleteLecture(@PathVariable Long lectureId) {
        lectureService.deleteLecture(lectureId);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }
}
