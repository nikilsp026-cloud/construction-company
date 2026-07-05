package com.construction.controller.admin;

import com.construction.entity.Testimonial;
import com.construction.service.ContactMessageService;
import com.construction.service.TestimonialService;
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
@RequestMapping("/admin/testimonials")
@RequiredArgsConstructor
public class AdminTestimonialController {

    private final TestimonialService testimonialService;
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
        model.addAttribute("testimonials", testimonialService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
        return "admin/testimonials/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("testimonial", new Testimonial());
        model.addAttribute("statuses", Testimonial.TestimonialStatus.values());
        return "admin/testimonials/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        model.addAttribute("testimonial", testimonialService.findById(id).orElseThrow());
        model.addAttribute("statuses", Testimonial.TestimonialStatus.values());
        return "admin/testimonials/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("testimonial") Testimonial testimonial,
                       BindingResult bindingResult,
                       RedirectAttributes ra,
                       Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("statuses", Testimonial.TestimonialStatus.values());
            model.addAttribute("errorMessage", "Please fix the highlighted errors and try again.");
            return "admin/testimonials/form";
        }
        testimonialService.save(testimonial);
        ra.addFlashAttribute("successMessage", "Testimonial saved.");
        return "redirect:/admin/testimonials";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        testimonialService.delete(id);
        ra.addFlashAttribute("successMessage", "Testimonial deleted.");
        return "redirect:/admin/testimonials";
    }
}
