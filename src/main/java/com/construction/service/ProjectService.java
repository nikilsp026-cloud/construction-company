package com.construction.service;

import com.construction.entity.Project;
import com.construction.entity.ProjectImage;
import com.construction.repository.ProjectImageRepository;
import com.construction.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final FileStorageService fileStorageService;
    private final HtmlSanitizerService htmlSanitizerService;

    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable p) {
        return projectRepository.findAll(p);
    }

    @Transactional(readOnly = true)
    public long count() {
        return projectRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<Project> findByStatus(Project.ProjectStatus s, Pageable p) {
        return projectRepository.findByStatus(s, p);
    }

    @Transactional(readOnly = true)
    public Page<Project> findByCategory(String cat, Pageable p) {
        return projectRepository.findByCategory(cat, p);
    }

    @Transactional(readOnly = true)
    public Page<Project> search(String kw, Pageable p) {
        return projectRepository.searchByKeyword(kw, p);
    }

    @Transactional(readOnly = true)
    public List<Project> findFeatured() {
        return projectRepository.findByFeaturedTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Project> findTop6() {
        return projectRepository.findTop6ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * Creates a new project, or updates an existing one.
     * <p>
     * On update we deliberately load the existing managed entity and copy the
     * editable fields onto it, rather than saving the transient object bound
     * straight from the form. The form does not (and should not) submit the
     * {@code images} collection, so a naive {@code repository.save(formObject)}
     * would merge in an empty list - and because {@code images} is mapped with
     * {@code orphanRemoval = true}, that silently deletes every gallery image
     * attached to the project on every single edit. Updating the managed
     * entity's scalar fields in place avoids touching the collection at all.
     */
    public Project save(Project p) {
        p.setDescription(htmlSanitizerService.sanitize(p.getDescription()));

        if (p.getId() == null) {
            return projectRepository.save(p);
        }

        Project existing = projectRepository.findById(p.getId())
                .orElseThrow(() -> new com.construction.exception.ResourceNotFoundException("Project", p.getId()));

        existing.setName(p.getName());
        existing.setDescription(p.getDescription());
        existing.setClientName(p.getClientName());
        existing.setLocation(p.getLocation());
        existing.setStartDate(p.getStartDate());
        existing.setCompletionDate(p.getCompletionDate());
        existing.setStatus(p.getStatus());
        existing.setBudget(p.getBudget());
        existing.setCategory(p.getCategory());
        existing.setFeatured(p.isFeatured());
        existing.setCompletionPercentage(p.getCompletionPercentage());
        // Thumbnail handling: null = leave unchanged, "" = explicitly cleared
        // (via the "remove thumbnail" checkbox), non-blank = new value. The old
        // file (if replaced or cleared) is deleted here, using the entity this
        // method already had to load - no second lookup needed in the controller.
        if (p.getThumbnail() != null) {
            String oldThumbnail = existing.getThumbnail();
            String newThumbnail = p.getThumbnail().isBlank() ? null : p.getThumbnail();
            if (oldThumbnail != null && !oldThumbnail.isBlank() && !oldThumbnail.equals(newThumbnail)) {
                fileStorageService.deleteFile(oldThumbnail);
            }
            existing.setThumbnail(newThumbnail);
        }

        return projectRepository.save(existing);
    }

    public void delete(Long id) {
        projectRepository.findById(id).ifPresent(project -> {
            // Delete thumbnail from disk
            if (project.getThumbnail() != null) {
                fileStorageService.deleteFile(project.getThumbnail());
            }
            // Delete all associated project images from disk
            List<ProjectImage> images = projectImageRepository.findByProjectIdOrderByCreatedAtAsc(id);
            for (ProjectImage image : images) {
                fileStorageService.deleteFile(image.getImagePath());
            }
            projectRepository.delete(project);
        });
    }

    @Transactional(readOnly = true)
    public List<String> findAllCategories() {
        return projectRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("ONGOING",   projectRepository.countByStatus(Project.ProjectStatus.ONGOING));
        counts.put("COMPLETED", projectRepository.countByStatus(Project.ProjectStatus.COMPLETED));
        counts.put("UPCOMING",  projectRepository.countByStatus(Project.ProjectStatus.UPCOMING));
        counts.put("ON_HOLD",   projectRepository.countByStatus(Project.ProjectStatus.ON_HOLD));
        return counts;
    }

    @Transactional(readOnly = true)
    public Page<Project> findFiltered(String status, String category, String keyword, Pageable p) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            return projectRepository.searchByKeyword(keyword, p);
        }

        Project.ProjectStatus statusEnum = parseStatus(status);
        boolean hasStatus   = statusEnum != null;
        boolean hasCategory = category != null && !category.isBlank();

        if (hasStatus && hasCategory) {
            return projectRepository.findByStatusAndCategory(statusEnum, category, p);
        }

        if (hasStatus) {
            return projectRepository.findByStatus(statusEnum, p);
        }

        if (hasCategory) {
            return projectRepository.findByCategory(category, p);
        }

        return projectRepository.findAll(p);
    }

    // An unrecognized status value (bad/stale query param) should fall back to
    // "no filter" for the public page rather than raising an error.
    private Project.ProjectStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return Project.ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
