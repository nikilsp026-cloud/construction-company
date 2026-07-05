package com.construction.controller.admin;

import com.construction.service.ContactMessageService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final WebsiteSettingService websiteSettingService;
    private final ContactMessageService contactMessageService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
    }

    @GetMapping
    public String settings(Model model) {
        addCommonAttributes(model);
        model.addAttribute("allSettings", websiteSettingService.findAll());
        return "admin/settings";
    }

    @PostMapping("/save")
    public String save(@RequestParam Map<String, String> params, RedirectAttributes ra) {
        params.remove("_csrf");
        websiteSettingService.saveAll(params);
        ra.addFlashAttribute("successMessage", "Settings saved successfully.");
        return "redirect:/admin/settings";
    }
}
