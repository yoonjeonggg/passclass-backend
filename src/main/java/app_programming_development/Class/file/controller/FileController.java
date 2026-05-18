package app_programming_development.Class.file.controller;

import app_programming_development.Class.dto.file.response.FileResponse;
import app_programming_development.Class.file.service.FileService;
import app_programming_development.Class.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 관련 API (Firebase Storage)")
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "Firebase Storage에 파일을 업로드합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 형식", content = @Content)
    })
    public ResponseEntity<ApiResponse<FileResponse>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        FileResponse result = fileService.upload(file);
        return ResponseEntity.ok(ApiResponse.ok(result, "파일이 Firebase에 업로드되었습니다."));
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "파일 정보 조회", description = "DB에 저장된 파일 메타데이터 및 URL을 조회합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> getFileInfo(@PathVariable Long fileId) {
        FileResponse result = fileService.getFileInfo(fileId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "파일 삭제", description = "Firebase 및 DB에서 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long fileId) throws IOException {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.ok(null, "파일이 삭제되었습니다."));
    }
}
