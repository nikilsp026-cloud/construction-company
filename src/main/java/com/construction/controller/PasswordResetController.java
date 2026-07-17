package com.construction.controller;

import com.construction.entity.User;
import com.construction.repository.UserRepository;
import com.construction.service.EmailService;
import com.construction.service.WebsiteSettingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final WebsiteSettingService websiteSettingService;

    private static final long TOKEN_VALID_MINUTES = 30;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email, RedirectAttributes ra, HttpServletRequest request) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES));
            userRepository.save(user);

            String resetLink = baseUrl(request) + "/reset-password?token=" + token;
            emailService.send(user.getEmail(), "Reset your password",
                    "<p>Click the link below to reset your admin password. This link expires in 30 minutes.</p>"
                            + "<p><a href=\"" + resetLink + "\">" + resetLink + "</a></p>"
                            + "<p>If you didn't request this, you can safely ignore this email.</p>");
        });
        // Same message whether or not the email exists - avoids leaking which emails are registered.
        ra.addFlashAttribute("successMessage", "If that email is registered, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model, RedirectAttributes ra) {
        if (findValidToken(token).isEmpty()) {
            ra.addFlashAttribute("errorMessage", "This reset link is invalid or has expired. Please request a new one.");
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes ra) {
        Optional<User> userOpt = findValidToken(token);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "This reset link is invalid or has expired. Please request a new one.");
            return "redirect:/forgot-password";
        }
        if (password == null || password.length() < 8) {
            ra.addFlashAttribute("errorMessage", "Password must be at least 8 characters.");
            return "redirect:/reset-password?token=" + token;
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/reset-password?token=" + token;
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        ra.addFlashAttribute("successMessage", "Your password has been reset. Please log in.");
        return "redirect:/login";
    }

    private Optional<User> findValidToken(String token) {
        return userRepository.findByResetToken(token)
                .filter(u -> u.getResetTokenExpiry() != null && u.getResetTokenExpiry().isAfter(LocalDateTime.now()));
    }

    private String baseUrl(HttpServletRequest request) {
        int port = request.getServerPort();
        boolean defaultPort = port == 80 || port == 443;
        return request.getScheme() + "://" + request.getServerName() + (defaultPort ? "" : ":" + port);
    }
}
