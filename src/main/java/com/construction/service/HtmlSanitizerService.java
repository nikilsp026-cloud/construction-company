package com.construction.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

/**
 * Whitelists safe HTML for admin-authored rich content (blog posts, project
 * descriptions) that is later rendered unescaped (th:utext) on public pages.
 */
@Service
public class HtmlSanitizerService {

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.STYLES)
            .and(Sanitizers.TABLES)
            .and(Sanitizers.IMAGES)
            .and(new HtmlPolicyBuilder()
                    .allowElements("h1", "h2", "h3", "h4", "h5", "h6", "hr")
                    .toFactory());

    public String sanitize(String html) {
        return html == null ? null : POLICY.sanitize(html);
    }
}
