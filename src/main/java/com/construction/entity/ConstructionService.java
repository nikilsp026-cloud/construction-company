package com.construction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title", "status"})
public class ConstructionService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Font Awesome icon class, e.g. "fas fa-hard-hat" */
    @Column(length = 100)
    private String icon;

    @Column(length = 500)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(name = "display_order")
    @Builder.Default
    private int displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ServiceStatus {
        ACTIVE, INACTIVE
    }

    /**
     * Alias for {@link #image}. admin/services/form.html references
     * "imageUrl" - kept as a read-only convenience getter so the template
     * doesn't fail with a PropertyNotFoundException.
     */
    public String getImageUrl() {
        return image;
    }
}
