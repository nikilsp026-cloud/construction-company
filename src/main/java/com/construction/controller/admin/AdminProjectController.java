package com.construction.controller.admin;

import com.construction.entity.Project;
import com.construction.service.ContactMessageService;
import com.construction.service.FileStorageService;
import com.construction.service.ProjectService;
import com.construction.service.WebsiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final ProjectService projectService;
    private final FileStorageService fileStorageService;
    private final ContactMessageService contactMessageService;
    private final WebsiteSettingService websiteSettingService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void addCommonAttributes(Model model) {
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        model.addAttribute("settings",   websiteSettingService.getAllAsMap());
    }

    // -------------------------------------------------------------------------
    // LIST
    // -------------------------------------------------------------------------

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        addCommonAttributes(model);

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projects = projectService.findAll(pageable);

        model.addAttribute("projects",    projects);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize",    size);

        return "admin/projects/list";
    }

    // -------------------------------------------------------------------------
    // CREATE FORM
    // -------------------------------------------------------------------------

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("project",  new Project());
        model.addAttribute("statuses", Project.ProjectStatus.values());
        return "admin/projects/form";
    }

    // -------------------------------------------------------------------------
    // EDIT FORM
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Project project = projectService.findById(id).orElse(null);
        if (project == null) {
            ra.addFlashAttribute("errorMessage", "Project not found.");
            return "redirect:/admin/projects";
        }
        addCommonAttributes(model);
        model.addAttribute("project",  project);
        model.addAttribute("statuses", Project.ProjectStatus.values());
        return "admin/projects/form";
    }

    // -------------------------------------------------------------------------
    // SAVE (create or update)
    // -------------------------------------------------------------------------

    @PostMapping("/save")
    public String save(
            @jakarta.validation.Valid @ModelAttribute Project project,
            BindingResult bindingResult,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "removeThumbnail", required = false) Boolean removeThumbnail,
            RedirectAttributes ra,
            Model model) throws java.io.IOException {

        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("statuses", Project.ProjectStatus.values());
            model.addAttribute("errorMessage", "Please fix the highlighted errors and try again.");
            return "admin/projects/form";
        }

        Project existingForThumbnail = (project.getId() != null)
                ? projectService.findById(project.getId()).orElse(null)
                : null;

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            // New file uploaded: replace whatever thumbnail existed before.
            if (existingForThumbnail != null && existingForThumbnail.getThumbnail() != null
                    && !existingForThumbnail.getThumbnail().isBlank()) {
                fileStorageService.deleteFile(existingForThumbnail.getThumbnail());
            }
            project.setThumbnail(fileStorageService.saveImage(thumbnailFile, "images"));
        } else if (Boolean.TRUE.equals(removeThumbnail) && existingForThumbnail != null) {
            // "Remove current thumbnail" checkbox: clear it out both on disk and in the DB.
            if (existingForThumbnail.getThumbnail() != null && !existingForThumbnail.getThumbnail().isBlank()) {
                fileStorageService.deleteFile(existingForThumbnail.getThumbnail());
            }
            project.setThumbnail("");
        }
        // Otherwise leave project.thumbnail as-is (null) so ProjectService.save()
        // knows to keep the existing value untouched.

        projectService.save(project);
        ra.addFlashAttribute("successMessage", "Project saved successfully.");
        return "redirect:/admin/projects";
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        projectService.delete(id);
        ra.addFlashAttribute("successMessage", "Project deleted successfully.");
        return "redirect:/admin/projects";
    }
}
