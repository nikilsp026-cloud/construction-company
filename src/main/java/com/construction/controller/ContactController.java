package com.construction.controller;

import com.construction.entity.ContactMessage;
import com.construction.service.ContactMessageService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String contact(Model model) {
        model.addAttribute("message", new ContactMessage());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "contact");
        return "contact";
    }

    @PostMapping
    public String submit(@ModelAttribute ContactMessage message, RedirectAttributes ra) {
        contactMessageService.save(message);
        ra.addFlashAttribute("successMessage", "Your message has been sent. We'll get back to you soon!");
        return "redirect:/contact";
    }
}
