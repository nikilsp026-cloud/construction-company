package com.construction.repository;

import com.construction.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Blog> findByStatus(Blog.BlogStatus status, Pageable pageable);

    List<Blog> findTop3ByStatusOrderByPublishedDateDesc(Blog.BlogStatus status);

    @Query("SELECT b FROM Blog b WHERE b.status = :status AND " +
           "LOWER(b.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    Page<Blog> findByStatusAndTag(@Param("status") Blog.BlogStatus status,
                                  @Param("tag") String tag,
                                  Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.status = :status AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchPublished(@Param("status") Blog.BlogStatus status,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    long countByStatus(Blog.BlogStatus status);
}
