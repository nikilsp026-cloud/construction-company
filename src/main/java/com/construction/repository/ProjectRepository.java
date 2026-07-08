package com.construction.repository;

import com.construction.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // Eagerly fetches the lazy `images` collection so the project-detail view
    // can render it without a live Hibernate session (open-in-view is disabled).
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Project> findByIdWithImages(@Param("id") Long id);
}
