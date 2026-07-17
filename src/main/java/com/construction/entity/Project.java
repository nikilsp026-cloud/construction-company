package com.construction.entity;

import com.construction.util.VideoUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_projects_status", columnList = "status"),
        @Index(name = "idx_projects_category", columnList = "category"),
        @Index(name = "idx_projects_featured", columnList = "featured")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "status"})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Project name is required.")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(length = 300)
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ONGOING;

    @Column(precision = 15, scale = 2)
    @PositiveOrZero(message = "Budget cannot be negative.")
    private BigDecimal budget;

    /** Path to the main thumbnail image */
    @Column(length = 500)
    private String thumbnail;

    /** YouTube (or other embeddable) video URL for a project walkthrough */
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** e.g. Residential, Commercial, Industrial, Infrastructure */
    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(name = "completion_percentage")
    @Builder.Default
    private int completionPercentage = 0;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Derived, non-persisted getter: converts the stored YouTube URL into an
     * embeddable player URL. Kept as a plain instance method (rather than a
     * T(...) static call from the template) because Thymeleaf's SpEL
     * evaluator forbids static-class access in several attribute contexts.
     */
    public String getVideoEmbedUrl() {
        return VideoUtil.toEmbedUrl(videoUrl);
    }

    public void addImage(ProjectImage image) {
        images.add(image);
        image.setProject(this);
    }

    public void removeImage(ProjectImage image) {
        images.remove(image);
        image.setProject(null);
    }

    public enum ProjectStatus {
        ONGOING, COMPLETED, UPCOMING, ON_HOLD
    }
}
