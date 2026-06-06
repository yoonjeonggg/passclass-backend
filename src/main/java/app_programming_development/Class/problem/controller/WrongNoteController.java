package app_programming_development.Class.problem.controller;

import app_programming_development.Class.dto.wrongnote.request.UpdateWrongNoteMemoRequest;
import app_programming_development.Class.dto.wrongnote.response.WrongNoteResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.problem.service.WrongNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong-notes")
@RequiredArgsConstructor
@Tag(name = "WrongNote", description = "오답노트 관련 API")
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    @GetMapping
    @Operation(summary = "내 오답노트 목록 조회")
    public ResponseEntity<ApiResponse<List<WrongNoteResponse>>> getMyWrongNotes() {
        return ResponseEntity.ok(ApiResponse.ok(wrongNoteService.getMyWrongNotes(), "조회되었습니다."));
    }

    @GetMapping("/favorites")
    @Operation(summary = "즐겨찾기한 오답노트 목록 조회")
    public ResponseEntity<ApiResponse<List<WrongNoteResponse>>> getMyFavoriteWrongNotes() {
        return ResponseEntity.ok(ApiResponse.ok(wrongNoteService.getMyFavoriteWrongNotes(), "조회되었습니다."));
    }

    @PatchMapping("/{wrongNoteId}/favorite")
    @Operation(summary = "오답노트 즐겨찾기 토글")
    public ResponseEntity<ApiResponse<WrongNoteResponse>> toggleFavorite(@PathVariable Long wrongNoteId) {
        return ResponseEntity.ok(ApiResponse.ok(wrongNoteService.toggleFavorite(wrongNoteId), "즐겨찾기가 변경되었습니다."));
    }

    @PatchMapping("/{wrongNoteId}/memo")
    @Operation(summary = "오답노트 메모 수정")
    public ResponseEntity<ApiResponse<WrongNoteResponse>> updateMemo(
            @PathVariable Long wrongNoteId,
            @RequestBody UpdateWrongNoteMemoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(wrongNoteService.updateMemo(wrongNoteId, request), "메모가 수정되었습니다."));
    }

    @DeleteMapping("/{wrongNoteId}")
    @Operation(summary = "오답노트 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteWrongNote(@PathVariable Long wrongNoteId) {
        wrongNoteService.deleteWrongNote(wrongNoteId);
        return ResponseEntity.ok(ApiResponse.ok("오답노트가 삭제되었습니다."));
    }
}
