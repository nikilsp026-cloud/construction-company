package com.construction.controller;

import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final WebsiteSettingService websiteSettingService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        return "login";
    }
}
