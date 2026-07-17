package com.construction.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forces the CSRF token (and its Set-Cookie) to be resolved at the very
 * start of the filter chain, before any response body is written.
 * <p>
 * Spring Security 6's default CSRF handling defers loading the token until
 * something actually reads it (BREACH-protection masking), and
 * CookieCsrfTokenRepository only writes its Set-Cookie header at that point.
 * On a large page (e.g. /contact, with a hero section, info cards, and a
 * long form before the hidden _csrf field), Thymeleaf can stream enough HTML
 * to make Tomcat auto-commit the response - flushing headers - before that
 * deferred read happens. Once committed, the Set-Cookie is silently
 * dropped: the page still renders a _csrf value, but with no matching
 * cookie, so every submission fails CSRF validation. Reading the token here
 * guarantees the cookie is queued while the response is still safely
 * uncommitted, regardless of page size.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
