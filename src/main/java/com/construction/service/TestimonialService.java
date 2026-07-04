package com.construction.service;

import com.construction.entity.Testimonial;
import com.construction.entity.Testimonial.TestimonialStatus;
import com.construction.repository.TestimonialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final FileStorageService fileStorageService;

    public TestimonialService(TestimonialRepository testimonialRepository, FileStorageService fileStorageService) {
        this.testimonialRepository = testimonialRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<Testimonial> findActive() {
        return testimonialRepository.findByStatusOrderByCreatedAtDesc(TestimonialStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Page<Testimonial> findAll(Pageable p) {
        return testimonialRepository.findAll(p);
    }

    @Transactional(readOnly = true)
    public Optional<Testimonial> findById(Long id) {
        return testimonialRepository.findById(id);
    }

    public Testimonial save(Testimonial t) {
        if (t.getId() != null && t.getPhoto() == null) {
            testimonialRepository.findById(t.getId())
                    .ifPresent(existing -> t.setPhoto(existing.getPhoto()));
        }
        return testimonialRepository.save(t);
    }

    public void delete(Long id) {
        testimonialRepository.findById(id).ifPresent(t -> {
            if (t.getPhoto() != null && !t.getPhoto().isBlank()) {
                fileStorageService.deleteFile(t.getPhoto());
            }
            testimonialRepository.delete(t);
        });
    }
}
