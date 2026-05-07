package app_programming_development.Class.like.controller;

import app_programming_development.Class.dto.like.response.LikeResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
@Tag(name = "Like", description = "강의 찜 관련 API")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{lectureId}/like")
    @Operation(summary = "강의 찜 등록/취소", description = "강의 찜 상태를 토글하는 API 입니다. 찜이 되어있으면 취소, 아니면 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 강의를 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(@PathVariable Long lectureId) {
        LikeResponse result = likeService.toggleLike(lectureId);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }
}
