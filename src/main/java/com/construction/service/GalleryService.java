package com.construction.service;

import com.construction.entity.Gallery;
import com.construction.repository.GalleryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final FileStorageService fileStorageService;

    public GalleryService(GalleryRepository galleryRepository, FileStorageService fileStorageService) {
        this.galleryRepository = galleryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<Gallery> findAll(Pageable p) {
        return galleryRepository.findAllByOrderByCreatedAtDesc(p);
    }

    @Transactional(readOnly = true)
    public Page<Gallery> findByCategory(String cat, Pageable p) {
        return galleryRepository.findByCategoryOrderByCreatedAtDesc(cat, p);
    }

    @Transactional(readOnly = true)
    public List<String> findAllCategories() {
        return galleryRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public Optional<Gallery> findById(Long id) {
        return galleryRepository.findById(id);
    }

    public Gallery save(Gallery g, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            // New image supplied: replace on disk and use the new path.
            String oldPath = null;
            if (g.getId() != null) {
                oldPath = galleryRepository.findById(g.getId()).map(Gallery::getImagePath).orElse(null);
            }
            String imagePath = fileStorageService.saveImage(imageFile, "images");
            g.setImagePath(imagePath);
            if (oldPath != null && !oldPath.equals(imagePath)) {
                fileStorageService.deleteFile(oldPath);
            }
        } else if (g.getId() != null) {
            // Editing an existing item without uploading a new file: the form only
            // submits title/category/description, so `g.imagePath` is null here.
            // image_path is NOT NULL in the database, so we must carry the existing
            // value forward instead of overwriting it with null.
            Gallery existing = galleryRepository.findById(g.getId())
                    .orElseThrow(() -> new com.construction.exception.ResourceNotFoundException("Gallery item", g.getId()));
            g.setImagePath(existing.getImagePath());
            g.setCreatedAt(existing.getCreatedAt());
        } else {
            throw new IllegalArgumentException("Please choose an image to upload.");
        }
        return galleryRepository.save(g);
    }

    public void delete(Long id) {
        galleryRepository.findById(id).ifPresent(gallery -> {
            fileStorageService.deleteFile(gallery.getImagePath());
            galleryRepository.delete(gallery);
        });
    }
}
