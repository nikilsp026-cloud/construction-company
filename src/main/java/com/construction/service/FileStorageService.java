package com.construction.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

/**
 * Uploads images to Cloudflare R2 (S3-compatible object storage) rather than
 * the container's local disk. Local disk on Render/Koyeb/most PaaS platforms
 * is ephemeral and gets wiped on every redeploy/restart, which was silently
 * losing every previously uploaded image - R2 survives that.
 */
@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    @Value("${app.r2.account-id}")
    private String accountId;

    @Value("${app.r2.access-key}")
    private String accessKey;

    @Value("${app.r2.secret-key}")
    private String secretKey;

    @Value("${app.r2.bucket}")
    private String bucket;

    @Value("${app.r2.public-url}")
    private String publicUrl;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                // R2 does not use AWS regions, but the SDK requires a value - "auto" is R2's documented placeholder.
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        log.info("R2 file storage initialised for bucket '{}'", bucket);
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
        // Never trust the client-supplied filename for the key itself - only its extension.
        String uniqueName = UUID.randomUUID() + extension;
        // subDir is developer-controlled ("images"/"docs"), never derived from user input.
        String key = subDir + "/" + uniqueName;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        String url = publicUrl.replaceAll("/$", "") + "/" + key;
        log.info("Uploaded file '{}' -> '{}'", originalFilename, url);
        return url;
    }

    public void deleteFile(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String prefix = publicUrl.replaceAll("/$", "") + "/";
        if (!url.startsWith(prefix)) {
            log.debug("Skipping delete for '{}': not an R2-hosted file", url);
            return;
        }
        String key = url.substring(prefix.length());
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            log.debug("Deleted R2 object '{}'", key);
        } catch (Exception e) {
            // Not fatal: the DB record is still updated/removed even if the physical
            // file couldn't be deleted. Log it so it's visible in monitoring instead
            // of silently vanishing.
            log.warn("Could not delete R2 object '{}': {}", key, e.getMessage());
        }
    }
}
