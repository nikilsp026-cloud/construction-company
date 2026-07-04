package com.construction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title", "category"})
public class Gallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    /** Category for masonry grid filtering, e.g. "Residential", "Commercial" */
    @Column(length = 100)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Alias for {@link #imagePath}. Several templates reference "imageUrl" -
     * kept as a read-only convenience getter so those templates don't fail
     * with a PropertyNotFoundException, without renaming the underlying
     * (already-migrated) database column.
     */
    public String getImageUrl() {
        return imagePath;
    }
}
