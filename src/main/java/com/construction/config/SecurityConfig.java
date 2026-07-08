package com.construction.config;

import com.construction.service.CustomUserDetailsService;
import com.construction.service.LoginRateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final LoginRateLimiterService loginRateLimiterService;
    private final Environment environment;

    /**
     * Secret used to sign remember-me tokens. Must be set via the
     * APP_REMEMBER_ME_KEY environment variable in production - never
     * hardcode secrets in source control.
     */
    @Value("${app.security.remember-me-key}")
    private String rememberMeKey;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                           LoginRateLimiterService loginRateLimiterService,
                           Environment environment) {
        this.customUserDetailsService = customUserDetailsService;
        this.loginRateLimiterService = loginRateLimiterService;
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(new CookieCsrfTokenRepository())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**").permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    loginRateLimiterService.recordSuccess(request.getRemoteAddr());
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                    loginRateLimiterService.recordFailure(request.getRemoteAddr());
                    response.sendRedirect(request.getContextPath() + "/login?error");
                })
                .permitAll()
            )
            .addFilterBefore(new LoginRateLimitFilter(loginRateLimiterService), UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key(rememberMeKey)
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; "
                        + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                        + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; "
                        + "font-src 'self' https://cdnjs.cloudflare.com https://fonts.gstatic.com; "
                        + "img-src 'self' data: https://placehold.co https://img.youtube.com; "
                        + "frame-src https://maps.google.com https://www.google.com https://www.youtube.com https://youtube.com; "
                        + "object-src 'none'; "
                        + "base-uri 'self'; "
                        + "frame-ancestors 'self'"
                ))
            );

        // Only enforce HTTPS in prod - local/dev runs over plain HTTP.
        // Actuator health/info stays reachable over HTTP too, since Koyeb's
        // internal health checks hit the container directly, bypassing the
        // HTTPS-terminating edge proxy.
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            http.requiresChannel(channel -> channel
                .requestMatchers("/actuator/**").requiresInsecure()
                .anyRequest().requiresSecure()
            );
        }

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
