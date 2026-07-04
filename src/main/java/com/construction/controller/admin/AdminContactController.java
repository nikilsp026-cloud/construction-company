package com.construction.controller.admin;

import com.construction.service.ContactMessageService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/contacts")
@RequiredArgsConstructor
public class AdminContactController {

    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "all") String filter,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {
        addCommonAttributes(model);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var messages = "unread".equals(filter)
                ? contactMessageService.findUnread(pageable)
                : contactMessageService.findAll(pageable);
        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        return "admin/contacts/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        var message = contactMessageService.findById(id).orElseThrow();
        contactMessageService.markAsRead(id);
        model.addAttribute("message", message);
        return "admin/contacts/view";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        contactMessageService.delete(id);
        ra.addFlashAttribute("successMessage", "Message deleted.");
        return "redirect:/admin/contacts";
    }
}
