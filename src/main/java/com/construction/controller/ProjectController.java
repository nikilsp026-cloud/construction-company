package com.construction.controller;

import com.construction.service.ProjectService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        model.addAttribute("projects", projectService.findFiltered(status, category, keyword, pageable));
        model.addAttribute("categories", projectService.findAllCategories());
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "projects");
        return "projects";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var project = projectService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("project", project);
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "projects");
        return "project-detail";
    }
}
