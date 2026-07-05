package com.construction.controller.admin;

import com.construction.entity.Blog;
import com.construction.repository.UserRepository;
import com.construction.service.BlogService;
import com.construction.service.ContactMessageService;
import com.construction.service.FileStorageService;
import com.construction.service.WebsiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogService blogService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        addCommonAttributes(model);
        model.addAttribute("posts", blogService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        return "admin/blog/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("blog", new Blog());
        model.addAttribute("statuses", Blog.BlogStatus.values());
        model.addAttribute("authors", userRepository.findAll());
        return "admin/blog/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        model.addAttribute("blog", blogService.findById(id).orElseThrow());
        model.addAttribute("statuses", Blog.BlogStatus.values());
        model.addAttribute("authors", userRepository.findAll());
        return "admin/blog/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("blog") Blog blog,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Long authorId,
                       @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                       RedirectAttributes ra,
                       Model model) throws java.io.IOException {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("statuses", Blog.BlogStatus.values());
            model.addAttribute("authors", userRepository.findAll());
            model.addAttribute("errorMessage", "Please fix the highlighted errors and try again.");
            return "admin/blog/form";
        }
        if (authorId != null) {
            blog.setAuthor(userRepository.findById(authorId).orElse(null));
        }
        if (blog.getStatus() == Blog.BlogStatus.PUBLISHED && blog.getPublishedDate() == null) {
            blog.setPublishedDate(LocalDateTime.now());
        }
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (blog.getId() != null) {
                blogService.findById(blog.getId()).ifPresent(existing -> {
                    if (existing.getThumbnail() != null && !existing.getThumbnail().isBlank()) {
                        fileStorageService.deleteFile(existing.getThumbnail());
                    }
                });
            }
            blog.setThumbnail(fileStorageService.saveImage(thumbnailFile, "images"));
        }
        // If no new file was uploaded, leave blog.thumbnail null - BlogService.save()
        // will carry the existing value forward on update.
        blogService.save(blog);
        ra.addFlashAttribute("successMessage", "Blog post saved.");
        return "redirect:/admin/blog";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        blogService.delete(id);
        ra.addFlashAttribute("successMessage", "Blog post deleted.");
        return "redirect:/admin/blog";
    }
}
