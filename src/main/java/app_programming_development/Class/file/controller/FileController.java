package app_programming_development.Class.file.controller;

import app_programming_development.Class.dto.file.response.FileResponse;
import app_programming_development.Class.file.service.FileService;
import app_programming_development.Class.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 관련 API")
public class FileController {

    private final FileService fileService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "파일 업로드 시 사용하는 API 입니다. 허용 확장자: jpg, jpeg, png, gif, mp4, avi, mkv, pdf / 최대 10MB")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 크기 초과", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<FileResponse>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        FileResponse result = fileService.upload(file);
        return ResponseEntity.ok(ApiResponse.ok(result, "파일이 업로드되었습니다."));
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "파일 정보 조회", description = "업로드된 파일 정보를 조회하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 파일을 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<FileResponse>> getFileInfo(@PathVariable Long fileId) {
        FileResponse result = fileService.getFileInfo(fileId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/download/{storedName}")
    @Operation(summary = "파일 다운로드", description = "파일 URL을 통해 파일을 다운로드하는 API 입니다.")
    public ResponseEntity<Resource> downloadFile(@PathVariable String storedName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(storedName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 파일을 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long fileId) throws IOException {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.ok(null, "파일이 삭제되었습니다."));
    }
}
