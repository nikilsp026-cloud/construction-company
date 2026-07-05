package com.construction.controller;

import com.construction.entity.QuoteRequest;
import com.construction.service.QuoteRequestService;
import com.construction.service.WebsiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quote")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteRequestService quoteRequestService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String quote(Model model) {
        model.addAttribute("quote", new QuoteRequest());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "quote");
        return "quote";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("quote") QuoteRequest quote, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("settings", websiteSettingService.getAllAsMap());
            model.addAttribute("activePage", "quote");
            return "quote";
        }
        quoteRequestService.save(quote);
        ra.addFlashAttribute("successMessage", "Your quote request has been submitted! We'll contact you within 24 hours.");
        return "redirect:/quote";
    }
}
