package com.construction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "email", "read"})
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please enter your name.")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @Email(message = "Please enter a valid email address.")
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 300)
    @Column(length = 300)
    private String subject;

    @NotBlank(message = "Please enter a message.")
    @Column(columnDefinition = "TEXT")
    private String message;

    /** Whether the admin has read/acknowledged this message */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
