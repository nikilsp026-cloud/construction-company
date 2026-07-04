package com.construction.controller;

import com.construction.service.TeamMemberService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/about")
@RequiredArgsConstructor
public class AboutController {

    private final TeamMemberService teamMemberService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String about(Model model) {
        model.addAttribute("team", teamMemberService.findActive());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "about");
        return "about";
    }
}
