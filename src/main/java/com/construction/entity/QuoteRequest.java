package com.construction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "email", "status"})
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String location;

    /** Budget range, e.g. "₹10,00,000 - ₹25,00,000" (Indian Rupees) */
    @Column(length = 100)
    private String budget;

    /** e.g. Residential, Commercial, Industrial, Renovation */
    @Column(name = "construction_type", length = 200)
    private String constructionType;

    /** Area in sq ft/sq m */
    @Column(length = 100)
    private String area;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** Path to the optional uploaded PDF */
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuoteStatus status = QuoteStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum QuoteStatus {
        PENDING, REVIEWED, APPROVED, REJECTED
    }
}
