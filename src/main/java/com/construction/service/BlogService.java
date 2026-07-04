package com.construction.service;

import com.construction.entity.Blog;
import com.construction.entity.Blog.BlogStatus;
import com.construction.repository.BlogRepository;
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
public class BlogService {

    private final BlogRepository blogRepository;
    private final FileStorageService fileStorageService;

    public BlogService(BlogRepository blogRepository, FileStorageService fileStorageService) {
        this.blogRepository = blogRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<Blog> findPublished(Pageable p) {
        return blogRepository.findByStatus(BlogStatus.PUBLISHED, p);
    }

    @Transactional(readOnly = true)
    public Page<Blog> findAll(Pageable p) {
        return blogRepository.findAll(p);
    }

    @Transactional(readOnly = true)
    public Optional<Blog> findById(Long id) {
        return blogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Blog> findBySlug(String slug) {
        return blogRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<Blog> findRecentPublished() {
        return blogRepository.findTop3ByStatusOrderByPublishedDateDesc(BlogStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public Page<Blog> search(String kw, Pageable p) {
        return blogRepository.searchPublished(Blog.BlogStatus.PUBLISHED, kw, p);
    }

    @Transactional(readOnly = true)
    public Page<Blog> findByTag(String tag, Pageable p) {
        return blogRepository.findByStatusAndTag(BlogStatus.PUBLISHED, tag, p);
    }

    public Blog save(Blog b) {
        if (b.getSlug() == null || b.getSlug().isBlank()) {
            String slug = b.getTitle()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-+|-+$", "");
            b.setSlug(slug);
        }
        if (b.getId() != null && b.getThumbnail() == null) {
            blogRepository.findById(b.getId())
                    .ifPresent(existing -> b.setThumbnail(existing.getThumbnail()));
        }
        return blogRepository.save(b);
    }

    public void delete(Long id) {
        blogRepository.findById(id).ifPresent(blog -> {
            if (blog.getThumbnail() != null && !blog.getThumbnail().isBlank()) {
                fileStorageService.deleteFile(blog.getThumbnail());
            }
            blogRepository.delete(blog);
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Long> countByStatus() {
        Map<String, Long> counts = new HashMap<>();
        for (BlogStatus status : BlogStatus.values()) {
            counts.put(status.name(), blogRepository.countByStatus(status));
        }
        return counts;
    }
}
