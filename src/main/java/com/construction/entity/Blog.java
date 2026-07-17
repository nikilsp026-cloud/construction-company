package com.construction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs", indexes = {
        @Index(name = "idx_blogs_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title", "slug", "status"})
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required.")
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String title;

    /** SEO-friendly URL slug, e.g. "top-construction-trends-2025" */
    @Column(unique = true, nullable = false, length = 300)
    private String slug;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    /** e.g. "Construction Tips", "Industry News" - shown as a badge on blog cards. */
    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String thumbnail;

    /** Comma-separated tags for filtering, e.g. "construction,tips,safety" */
    @Column(length = 500)
    private String tags;

    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Column(name = "seo_description", columnDefinition = "TEXT")
    private String seoDescription;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlogStatus status = BlogStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String[] getTagArray() {
        if (tags == null || tags.isBlank()) return new String[0];
        return tags.split(",");
    }

    /**
     * Estimated reading time in whole minutes, based on word count of the
     * article body at a standard ~200 words/minute. Always in sync with the
     * content (unlike a manually-entered value), and never null.
     */
    public Integer getReadTime() {
        if (content == null || content.isBlank()) {
            return null;
        }
        String plainText = content.replaceAll("<[^>]*>", " ");
        int wordCount = plainText.trim().isEmpty() ? 0 : plainText.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }

    public enum BlogStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
