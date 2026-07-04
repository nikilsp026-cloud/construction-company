package com.construction.controller;

import com.construction.service.GalleryService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String gallery(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var items = (category != null && !category.isBlank())
                ? galleryService.findByCategory(category, pageable)
                : galleryService.findAll(pageable);

        model.addAttribute("items", items);
        model.addAttribute("categories", galleryService.findAllCategories());
        model.addAttribute("currentCategory", category);
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "gallery");
        return "gallery";
    }
}
