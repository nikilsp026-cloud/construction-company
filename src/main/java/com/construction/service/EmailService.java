package com.construction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends transactional email via Resend's HTTP API. If RESEND_API_KEY isn't
 * configured, sending is skipped with a warning rather than failing the
 * calling request - email is a nice-to-have on top of the core flows
 * (password reset, lead alerts), not a hard dependency for them to work.
 */
@Slf4j
@Service
public class EmailService {

    @Value("${app.email.resend-api-key}")
    private String apiKey;

    @Value("${app.email.from-address}")
    private String fromAddress;

    private final RestClient restClient = RestClient.create("https://api.resend.com");

    public void send(String to, String subject, String htmlBody) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY not configured; skipping email to '{}' (subject: {})", to, subject);
            return;
        }
        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", fromAddress,
                            "to", List.of(to),
                            "subject", subject,
                            "html", htmlBody
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email sent to '{}' (subject: {})", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to '{}' (subject: {}): {}", to, subject, e.getMessage());
        }
    }
}
