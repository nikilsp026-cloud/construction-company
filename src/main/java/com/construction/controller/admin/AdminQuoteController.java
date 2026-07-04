package com.construction.controller.admin;

import com.construction.entity.QuoteRequest;
import com.construction.service.ContactMessageService;
import com.construction.service.QuoteRequestService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/quotes")
@RequiredArgsConstructor
public class AdminQuoteController {

    private final QuoteRequestService quoteRequestService;
    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
        model.addAttribute("statuses", QuoteRequest.QuoteStatus.values());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {
        addCommonAttributes(model);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var quotes = (status != null && !status.isBlank())
                ? quoteRequestService.findByStatus(QuoteRequest.QuoteStatus.valueOf(status), pageable)
                : quoteRequestService.findAll(pageable);
        model.addAttribute("quotes", quotes);
        model.addAttribute("currentStatus", status);
        return "admin/quotes/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        model.addAttribute("quote", quoteRequestService.findById(id).orElseThrow());
        return "admin/quotes/view";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam QuoteRequest.QuoteStatus status,
                               RedirectAttributes ra) {
        quoteRequestService.updateStatus(id, status);
        ra.addFlashAttribute("successMessage", "Status updated to " + status);
        return "redirect:/admin/quotes/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        quoteRequestService.delete(id);
        ra.addFlashAttribute("successMessage", "Quote request deleted.");
        return "redirect:/admin/quotes";
    }
}
