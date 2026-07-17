package com.construction.controller;

import com.construction.entity.QuoteRequest;
import com.construction.service.EmailService;
import com.construction.service.QuoteRequestService;
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
@RequestMapping("/quote")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteRequestService quoteRequestService;
    private final WebsiteSettingService websiteSettingService;
    private final EmailService emailService;

    @Value("${app.email.lead-alert-to}")
    private String leadAlertTo;

    @GetMapping
    public String quote(Model model) {
        model.addAttribute("quote", new QuoteRequest());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "quote");
        model.addAttribute("formRenderedAt", System.currentTimeMillis());
        return "quote";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("quote") QuoteRequest quote, BindingResult result,
                         @RequestParam(required = false) String website,
                         @RequestParam(required = false) String formRenderedAt,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("settings", websiteSettingService.getAllAsMap());
            model.addAttribute("activePage", "quote");
            model.addAttribute("formRenderedAt", System.currentTimeMillis());
            return "quote";
        }
        // Spam is accepted silently (same success message, nothing saved) so
        // bots get no signal to adapt their behaviour.
        if (!SpamGuard.isLikelyBot(website, formRenderedAt)) {
            quoteRequestService.save(quote);
            if (leadAlertTo != null && !leadAlertTo.isBlank()) {
                emailService.send(leadAlertTo, "New quote request: " + quote.getName(),
                        "<p><strong>Name:</strong> " + quote.getName() + "</p>"
                                + "<p><strong>Email:</strong> " + (quote.getEmail() != null ? quote.getEmail() : "—") + "</p>"
                                + "<p><strong>Phone:</strong> " + (quote.getPhone() != null ? quote.getPhone() : "—") + "</p>"
                                + "<p><strong>Location:</strong> " + (quote.getLocation() != null ? quote.getLocation() : "—") + "</p>"
                                + "<p><strong>Construction Type:</strong> " + (quote.getConstructionType() != null ? quote.getConstructionType() : "—") + "</p>"
                                + "<p><strong>Area:</strong> " + (quote.getArea() != null ? quote.getArea() : "—") + "</p>"
                                + "<p><strong>Budget:</strong> " + (quote.getBudget() != null ? quote.getBudget() : "—") + "</p>"
                                + "<p><strong>Message:</strong><br/>" + (quote.getMessage() != null ? quote.getMessage() : "—") + "</p>");
            }
        }
        ra.addFlashAttribute("successMessage", "Your quote request has been submitted! We'll contact you within 24 hours.");
        return "redirect:/quote";
    }
}
