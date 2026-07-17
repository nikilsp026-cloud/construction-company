package com.construction.controller;

import com.construction.entity.ContactMessage;
import com.construction.service.ContactMessageService;
import com.construction.service.EmailService;
import com.construction.service.WebsiteSettingService;
import com.construction.util.SpamGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;
    private final EmailService emailService;

    @Value("${app.email.lead-alert-to}")
    private String leadAlertTo;

    @GetMapping
    public String contact(Model model) {
        model.addAttribute("contactMessage", new ContactMessage());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "contact");
        model.addAttribute("formRenderedAt", System.currentTimeMillis());
        return "contact";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("contactMessage") ContactMessage message, BindingResult result,
                         @RequestParam(required = false) String website,
                         @RequestParam(required = false) String formRenderedAt,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("settings", websiteSettingService.getAllAsMap());
            model.addAttribute("activePage", "contact");
            model.addAttribute("formRenderedAt", System.currentTimeMillis());
            return "contact";
        }
        // Spam is accepted silently (same success message, nothing saved) so
        // bots get no signal to adapt their behaviour.
        if (!SpamGuard.isLikelyBot(website, formRenderedAt)) {
            contactMessageService.save(message);
            if (leadAlertTo != null && !leadAlertTo.isBlank()) {
                emailService.send(leadAlertTo, "New contact message: " + message.getName(),
                        "<p><strong>Name:</strong> " + message.getName() + "</p>"
                                + "<p><strong>Email:</strong> " + (message.getEmail() != null ? message.getEmail() : "—") + "</p>"
                                + "<p><strong>Phone:</strong> " + (message.getPhone() != null ? message.getPhone() : "—") + "</p>"
                                + "<p><strong>Subject:</strong> " + (message.getSubject() != null ? message.getSubject() : "—") + "</p>"
                                + "<p><strong>Message:</strong><br/>" + message.getMessage() + "</p>");
            }
        }
        ra.addFlashAttribute("successMessage", "Your message has been sent. We'll get back to you soon!");
        return "redirect:/contact";
    }
}
