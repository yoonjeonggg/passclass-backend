package app_programming_development.Class.file.service;

import app_programming_development.Class.dto.file.response.FileResponse;
import app_programming_development.Class.exceptions.badRequest.EmptyFileException;
import app_programming_development.Class.exceptions.badRequest.FileSizeExceededException;
import app_programming_development.Class.exceptions.badRequest.InvalidFileExtensionException;
import app_programming_development.Class.exceptions.badRequest.InvalidFileTypeException;
import app_programming_development.Class.exceptions.notFound.UploadedFileNotFoundException;
import app_programming_development.Class.file.entity.Files;
import app_programming_development.Class.file.repository.FileRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final SecurityUtils securityUtils;
    private final Bucket bucket; // FirebaseConfig에서 빈으로 등록한 Bucket 주입

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "mp4", "avi", "mkv", "pdf"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Transactional
    public FileResponse upload(MultipartFile file) throws IOException {
        validateFile(file);

        Users uploader = securityUtils.getCurrentUser();
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID() + "." + extension;

        // Firebase Storage에 업로드
        Blob blob = bucket.create(storedName, file.getInputStream(), file.getContentType());

        // Firebase Storage의 공개 URL 생성
        String fileUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucket.getName(), storedName);

        Files savedFile = fileRepository.save(Files.builder()
                .uploader(uploader)
                .originalName(originalName)
                .storedName(storedName)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build());

        log.info("File uploaded to Firebase: fileId={}, originalName={}, uploaderId={}",
                savedFile.getId(), originalName, uploader.getId());
        return FileResponse.from(savedFile);
    }

    @Transactional(readOnly = true)
    public FileResponse getFileInfo(Long fileId) {
        Files file = fileRepository.findById(fileId)
                .orElseThrow(UploadedFileNotFoundException::new);
        return FileResponse.from(file);
    }

    @Transactional
    public void deleteFile(Long fileId) {
        Files file = fileRepository.findById(fileId)
                .orElseThrow(UploadedFileNotFoundException::new);

        // Firebase Storage에서 파일 삭제
        Blob blob = bucket.get(file.getStoredName());
        if (blob != null) {
            blob.delete();
        }

        fileRepository.delete(file);
        log.info("File deleted from Firebase: fileId={}, storedName={}", fileId, file.getStoredName());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException();
        }
        String extension = getExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileTypeException();
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new InvalidFileExtensionException();
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
