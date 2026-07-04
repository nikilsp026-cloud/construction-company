package com.construction.controller;

import com.construction.service.ConstructionServiceService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServicePageController {

    private final ConstructionServiceService constructionServiceService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String services(Model model) {
        model.addAttribute("services", constructionServiceService.findAll());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "services");
        return "services";
    }
}
