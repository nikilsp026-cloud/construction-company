package com.construction.controller.admin;

import com.construction.entity.QuoteRequest;
import com.construction.service.ContactMessageService;
import com.construction.service.ConstructionServiceService;
import com.construction.service.ProjectService;
import com.construction.service.QuoteRequestService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProjectService projectService;
    private final ConstructionServiceService constructionServiceService;
    private final ContactMessageService contactMessageService;
    private final QuoteRequestService quoteRequestService;
    private final WebsiteSettingService websiteSettingService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {

        // --- Sidebar badge & global settings ---
        long unreadCount = contactMessageService.countUnread();
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("settings", websiteSettingService.getAllAsMap());

        // --- Top-level stat cards ---
        long projectCount = projectService.findAll(PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        long serviceCount = constructionServiceService.countActive();
        long messageCount = unreadCount;
        long quoteCount   = quoteRequestService.countByStatus(QuoteRequest.QuoteStatus.PENDING);

        model.addAttribute("projectCount",  projectCount);
        model.addAttribute("serviceCount",  serviceCount);
        model.addAttribute("messageCount",  messageCount);
        model.addAttribute("quoteCount",    quoteCount);

        // --- Project status breakdown map (ONGOING, COMPLETED, UPCOMING, ON_HOLD) ---
        model.addAttribute("projectStatusStats", projectService.countByStatus());

        // --- Quote status breakdown map (PENDING, REVIEWED, APPROVED, REJECTED) ---
        model.addAttribute("quoteStatusStats", quoteRequestService.statusSummary());

        // --- Recent data ---
        model.addAttribute("recentProjects",
                projectService.findTop6());

        model.addAttribute("recentMessages",
                contactMessageService.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))));

        return "admin/dashboard";
    }
}
