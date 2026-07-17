package com.construction.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
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

import java.io.ByteArrayOutputStream;
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
    private static final int MAX_DIMENSION = 1600; // px, longest side
    // GIF (animation) and WebP have no built-in Java ImageIO reader/writer,
    // so they're uploaded as-is rather than resized/recompressed.
    private static final Set<String> RESIZABLE_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg"
    );

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

        byte[] bytes = RESIZABLE_CONTENT_TYPES.contains(contentType.toLowerCase())
                ? resizeAndCompress(file, contentType)
                : file.getBytes();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes)
        );

        String url = publicUrl.replaceAll("/$", "") + "/" + key;
        log.info("Uploaded file '{}' ({} bytes) -> '{}'", originalFilename, bytes.length, url);
        return url;
    }

    /**
     * Downscales to at most {@link #MAX_DIMENSION}px on the longest side and
     * re-encodes with moderate compression, so a multi-MB phone photo isn't
     * served at full size to every visitor. Never upscales smaller images.
     * Falls back to the original bytes if the image can't be decoded, so a
     * malformed/unusual file doesn't block the whole upload.
     */
    private byte[] resizeAndCompress(MultipartFile file, String contentType) throws IOException {
        String formatName = contentType.equalsIgnoreCase("image/png") ? "png" : "jpg";
        try (var in = file.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(in)
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .outputFormat(formatName)
                    .outputQuality(0.82)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.warn("Could not resize image '{}', uploading original: {}", file.getOriginalFilename(), e.getMessage());
            return file.getBytes();
        }
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
