package com.construction.repository;

import com.construction.entity.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    Page<Gallery> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    @Query("SELECT DISTINCT g.category FROM Gallery g WHERE g.category IS NOT NULL ORDER BY g.category")
    List<String> findAllCategories();

    @Query("SELECT g FROM Gallery g WHERE " +
           "LOWER(g.title) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           "OR LOWER(g.category) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<Gallery> searchByKeyword(String keyword, Pageable pageable);

    Page<Gallery> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
