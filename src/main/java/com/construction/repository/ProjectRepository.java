package com.construction.repository;

import com.construction.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByFeaturedTrueOrderByCreatedAtDesc();

    Page<Project> findByStatus(Project.ProjectStatus status, Pageable pageable);

    Page<Project> findByCategory(String category, Pageable pageable);

    Page<Project> findByStatusAndCategory(Project.ProjectStatus status, String category, Pageable pageable);

    List<Project> findTop6ByOrderByCreatedAtDesc();

    long countByStatus(Project.ProjectStatus status);

    long countByFeaturedTrue();

    @Query("SELECT DISTINCT p.category FROM Project p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(p.clientName) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<Project> searchByKeyword(String keyword, Pageable pageable);
}
