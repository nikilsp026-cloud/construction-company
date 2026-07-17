package com.construction.controller;

import com.construction.entity.Blog;
import com.construction.entity.Project;
import com.construction.repository.BlogRepository;
import com.construction.repository.ProjectRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

/**
 * Generates sitemap.xml on the fly from live data, rather than a static file
 * that would silently go stale every time a project or blog post is added.
 */
@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final ProjectRepository projectRepository;
    private final BlogRepository blogRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String[] STATIC_PATHS = {
            "/", "/about", "/services", "/projects", "/gallery", "/blog", "/contact", "/quote"
    };

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + (isDefaultPort(request) ? "" : ":" + request.getServerPort());

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (String path : STATIC_PATHS) {
            appendUrl(xml, baseUrl, path, null);
        }

        for (Project p : projectRepository.findAll()) {
            String lastMod = p.getUpdatedAt() != null ? p.getUpdatedAt().format(DATE_FORMAT) : null;
            appendUrl(xml, baseUrl, "/projects/" + p.getId(), lastMod);
        }

        for (Blog b : blogRepository.findByStatus(Blog.BlogStatus.PUBLISHED, Pageable.unpaged())) {
            String lastMod = b.getUpdatedAt() != null ? b.getUpdatedAt().format(DATE_FORMAT) : null;
            appendUrl(xml, baseUrl, "/blog/" + b.getSlug(), lastMod);
        }

        xml.append("</urlset>\n");

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml.toString());
    }

    private boolean isDefaultPort(HttpServletRequest request) {
        int port = request.getServerPort();
        return port == 80 || port == 443;
    }

    private void appendUrl(StringBuilder xml, String baseUrl, String path, String lastMod) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(baseUrl + path)).append("</loc>\n");
        if (lastMod != null) {
            xml.append("    <lastmod>").append(lastMod).append("</lastmod>\n");
        }
        xml.append("  </url>\n");
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
