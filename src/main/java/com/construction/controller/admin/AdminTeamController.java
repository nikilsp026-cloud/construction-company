package com.construction.controller.admin;

import com.construction.entity.TeamMember;
import com.construction.service.ContactMessageService;
import com.construction.service.FileStorageService;
import com.construction.service.TeamMemberService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/team")
@RequiredArgsConstructor
public class AdminTeamController {

    private final TeamMemberService teamMemberService;
    private final FileStorageService fileStorageService;
    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings", websiteSettingService.getAllAsMap());
    }

    @GetMapping
    public String list(Model model) {
        addCommonAttributes(model);
        model.addAttribute("members", teamMemberService.findAll());
        return "admin/team/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("member", new TeamMember());
        return "admin/team/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        model.addAttribute("member", teamMemberService.findById(id).orElseThrow());
        return "admin/team/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute TeamMember member,
                       @RequestParam(required = false) MultipartFile photoFile,
                       RedirectAttributes ra) {
        try {
            if (photoFile != null && !photoFile.isEmpty()) {
                String path = fileStorageService.saveImage(photoFile, "images");
                member.setPhoto(path);
            }
            teamMemberService.save(member);
            ra.addFlashAttribute("successMessage", "Team member saved.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/team";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        teamMemberService.delete(id);
        ra.addFlashAttribute("successMessage", "Team member deleted.");
        return "redirect:/admin/team";
    }
}
