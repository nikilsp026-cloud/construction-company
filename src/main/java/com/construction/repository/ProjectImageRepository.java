package com.construction.repository;

import com.construction.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

    List<ProjectImage> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    @Transactional
    void deleteByProjectId(Long projectId);

    long countByProjectId(Long projectId);
}
