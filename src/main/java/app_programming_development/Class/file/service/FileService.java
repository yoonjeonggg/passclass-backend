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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final SecurityUtils securityUtils;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8009}")
    private String baseUrl;

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

        Path uploadPath = Paths.get(uploadDir);
        java.nio.file.Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(storedName);
        java.nio.file.Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = baseUrl + "/api/files/download/" + storedName;

        Files savedFile = fileRepository.save(Files.builder()
                .uploader(uploader)
                .originalName(originalName)
                .storedName(storedName)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build());

        return FileResponse.from(savedFile);
    }

    @Transactional(readOnly = true)
    public FileResponse getFileInfo(Long fileId) {
        Files file = fileRepository.findById(fileId)
                .orElseThrow(UploadedFileNotFoundException::new);
        return FileResponse.from(file);
    }

    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        Files file = fileRepository.findById(fileId)
                .orElseThrow(UploadedFileNotFoundException::new);

        Path filePath = Paths.get(uploadDir).resolve(file.getStoredName());
        java.nio.file.Files.deleteIfExists(filePath);
        fileRepository.delete(file);
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
