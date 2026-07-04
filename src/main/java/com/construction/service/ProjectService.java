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

    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable p) {
        return projectRepository.findAll(p);
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

    public Project save(Project p) {
        return projectRepository.save(p);
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
        boolean hasStatus   = status   != null && !status.isBlank();
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasKeyword  = keyword  != null && !keyword.isBlank();

        if (hasKeyword) {
            return projectRepository.searchByKeyword(keyword, p);
        }

        if (hasStatus && hasCategory) {
            Project.ProjectStatus statusEnum = Project.ProjectStatus.valueOf(status.toUpperCase());
            return projectRepository.findByStatusAndCategory(statusEnum, category, p);
        }

        if (hasStatus) {
            Project.ProjectStatus statusEnum = Project.ProjectStatus.valueOf(status.toUpperCase());
            return projectRepository.findByStatus(statusEnum, p);
        }

        if (hasCategory) {
            return projectRepository.findByCategory(category, p);
        }

        return projectRepository.findAll(p);
    }
}
