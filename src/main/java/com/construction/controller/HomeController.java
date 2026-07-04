package com.construction.controller;

import com.construction.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProjectService projectService;
    private final ConstructionServiceService constructionServiceService;
    private final TestimonialService testimonialService;
    private final TeamMemberService teamMemberService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProjects", projectService.findFeatured());
        model.addAttribute("services", constructionServiceService.findActive());
        model.addAttribute("testimonials", testimonialService.findActive());
        model.addAttribute("team", teamMemberService.findActive());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "home");
        return "index";
    }
}
