package com.construction.config;

import com.construction.service.LoginRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Blocks POST /login attempts from an IP that has already failed too many
 * times recently, before Spring Security spends a bcrypt comparison on them.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimiterService rateLimiter;

    public LoginRateLimitFilter(LoginRateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())
                && rateLimiter.isBlocked(request.getRemoteAddr())) {
            response.sendRedirect(request.getContextPath() + "/login?error=locked");
            return;
        }
        chain.doFilter(request, response);
    }
}
