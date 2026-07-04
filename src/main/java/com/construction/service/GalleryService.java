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

    public Gallery save(Gallery g, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.saveImage(imageFile, "images");
            g.setImagePath(imagePath);
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
