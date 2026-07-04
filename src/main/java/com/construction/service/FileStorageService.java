package com.construction.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadDir, "images"));
        Files.createDirectories(Paths.get(uploadDir, "docs"));
        log.info("File storage initialised at '{}'", Paths.get(uploadDir).toAbsolutePath());
    }

    public String saveImage(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a file to upload.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File is too large. Maximum allowed size is 10 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type. Please upload a PNG, JPG, WebP or GIF image.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                : "";
        // Never trust the client-supplied filename for the path itself - only its extension.
        String uniqueName = UUID.randomUUID() + extension;

        // subDir is developer-controlled ("images"/"docs"), never derived from user input,
        // so no path-traversal risk here - but we still normalize defensively.
        Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = baseDir.resolve(subDir).resolve(uniqueName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid upload destination.");
        }

        Files.createDirectories(target.getParent());
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored uploaded file '{}' -> '{}'", originalFilename, target);
        return "/uploads/" + subDir + "/" + uniqueName;
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path full = Paths.get(uploadDir).resolve(relativePath.replaceFirst("^/uploads/", ""));
            boolean deleted = Files.deleteIfExists(full);
            if (deleted) {
                log.debug("Deleted file '{}'", full);
            } else {
                log.debug("File '{}' was already absent, nothing to delete", full);
            }
        } catch (IOException e) {
            // Not fatal: the DB record is still updated/removed even if the physical
            // file couldn't be deleted (e.g. permissions, already gone). Log it so
            // it's visible in monitoring instead of silently vanishing.
            log.warn("Could not delete file '{}': {}", relativePath, e.getMessage());
        }
    }
}
