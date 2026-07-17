package com.construction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "testimonials", indexes = {
        @Index(name = "idx_testimonials_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "customerName", "rating", "status"})
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required.")
    @Size(max = 200)
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(length = 500)
    private String photo;

    @Column(length = 200)
    private String company;

    /** Star rating 1–5 */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    @Builder.Default
    private int rating = 5;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TestimonialStatus status = TestimonialStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TestimonialStatus {
        ACTIVE, INACTIVE
    }
}
