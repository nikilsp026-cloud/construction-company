package com.construction.controller.admin;

import com.construction.entity.ConstructionService;
import com.construction.service.ConstructionServiceService;
import com.construction.service.ContactMessageService;
import com.construction.service.FileStorageService;
import com.construction.service.WebsiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ConstructionServiceService constructionServiceService;
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
        model.addAttribute("services", constructionServiceService.findAll());
        return "admin/services/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommonAttributes(model);
        model.addAttribute("service", new ConstructionService());
        model.addAttribute("statuses", ConstructionService.ServiceStatus.values());
        return "admin/services/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        addCommonAttributes(model);
        model.addAttribute("service", constructionServiceService.findById(id).orElseThrow());
        model.addAttribute("statuses", ConstructionService.ServiceStatus.values());
        return "admin/services/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("service") ConstructionService service,
                       BindingResult bindingResult,
                       @RequestParam(required = false) MultipartFile imageFile,
                       @RequestParam(required = false, defaultValue = "false") boolean removeImage,
                       RedirectAttributes ra,
                       Model model) throws java.io.IOException {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("statuses", ConstructionService.ServiceStatus.values());
            model.addAttribute("errorMessage", "Please fix the highlighted errors and try again.");
            return "admin/services/form";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String path = fileStorageService.saveImage(imageFile, "images");
            service.setImage(path);
        } else if (removeImage && service.getId() != null) {
            constructionServiceService.findById(service.getId())
                    .map(ConstructionService::getImage)
                    .filter(image -> !image.isBlank())
                    .ifPresent(fileStorageService::deleteFile);
            service.setImage("");
        }
        constructionServiceService.save(service);
        ra.addFlashAttribute("successMessage", "Service saved successfully.");
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        constructionServiceService.delete(id);
        ra.addFlashAttribute("successMessage", "Service deleted.");
        return "redirect:/admin/services";
    }
}
