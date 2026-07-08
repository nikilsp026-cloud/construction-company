package com.construction.controller.admin;

import com.construction.entity.HomepageVideo;
import com.construction.service.ContactMessageService;
import com.construction.service.HomepageVideoService;
import com.construction.service.WebsiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/videos")
@RequiredArgsConstructor
public class AdminHomepageVideoController {

    private final HomepageVideoService homepageVideoService;
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
        model.addAttribute("videos", homepageVideoService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        return "admin/videos/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("video", new HomepageVideo());
        model.addAttribute("statuses", HomepageVideo.VideoStatus.values());
        return "admin/videos/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        model.addAttribute("video", homepageVideoService.findById(id).orElseThrow());
        model.addAttribute("statuses", HomepageVideo.VideoStatus.values());
        return "admin/videos/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("video") HomepageVideo video,
                        BindingResult bindingResult,
                        RedirectAttributes ra,
                        Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("statuses", HomepageVideo.VideoStatus.values());
            model.addAttribute("errorMessage", "Please fix the highlighted errors and try again.");
            return "admin/videos/form";
        }
        homepageVideoService.save(video);
        ra.addFlashAttribute("successMessage", "Video saved.");
        return "redirect:/admin/videos";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        homepageVideoService.delete(id);
        ra.addFlashAttribute("successMessage", "Video deleted.");
        return "redirect:/admin/videos";
    }
}
