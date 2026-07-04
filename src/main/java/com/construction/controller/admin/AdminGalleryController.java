package com.construction.controller.admin;

import com.construction.entity.Gallery;
import com.construction.service.ContactMessageService;
import com.construction.service.GalleryService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/gallery")
@RequiredArgsConstructor
public class AdminGalleryController {

    private final GalleryService galleryService;
    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {
        addCommonAttributes(model);
        model.addAttribute("items", galleryService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        return "admin/gallery/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("item", new Gallery());
        return "admin/gallery/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return galleryService.findById(id)
                .map(item -> {
                    addCommonAttributes(model);
                    model.addAttribute("item", item);
                    return "admin/gallery/form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Gallery item not found.");
                    return "redirect:/admin/gallery";
                });
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Gallery item,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        try {
            galleryService.save(item, imageFile);
            ra.addFlashAttribute("successMessage", "Gallery item saved.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/gallery";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        galleryService.delete(id);
        ra.addFlashAttribute("successMessage", "Gallery item deleted.");
        return "redirect:/admin/gallery";
    }
}
