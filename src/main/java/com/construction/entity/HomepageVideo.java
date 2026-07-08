package com.construction.entity;

import com.construction.util.VideoUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "homepage_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title", "status"})
public class HomepageVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required.")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "YouTube video URL is required.")
    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VideoStatus status = VideoStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Derived, non-persisted: embeddable player URL for the stored video link. */
    public String getEmbedUrl() {
        return VideoUtil.toEmbedUrl(videoUrl);
    }

    /** Derived, non-persisted: YouTube-hosted thumbnail, so no server storage is needed. */
    public String getThumbnailUrl() {
        return VideoUtil.toThumbnailUrl(videoUrl);
    }

    public enum VideoStatus {
        ACTIVE, INACTIVE
    }
}
