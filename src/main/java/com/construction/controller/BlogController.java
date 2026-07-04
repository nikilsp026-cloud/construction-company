package com.construction.controller;

import com.construction.entity.Blog;
import com.construction.service.BlogService;
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
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());
        var posts = (keyword != null && !keyword.isBlank())
                ? blogService.search(keyword, pageable)
                : (tag != null && !tag.isBlank())
                        ? blogService.findByTag(tag, pageable)
                        : blogService.findPublished(pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("recentPosts", blogService.findRecentPublished());
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentTag", tag);
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "blog");
        return "blog";
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        var post = blogService.findBySlug(slug)
                .filter(b -> b.getStatus() == Blog.BlogStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", blogService.findRecentPublished());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("activePage", "blog");
        return "blog-detail";
    }
}
