package com.construction.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Key-value store for runtime-editable site settings (company name, social links, etc.)
 * Avoid caching these in-process; always read from DB so admin changes take effect immediately.
 */
@Entity
@Table(name = "website_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "settingKey"})
public class WebsiteSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", unique = true, nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    /**
     * Hint for the admin UI: "text", "textarea", "image", "email", "url", "boolean"
     */
    @Column(name = "setting_type", length = 50)
    private String settingType;
}
