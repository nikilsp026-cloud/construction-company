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

    public TestimonialService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
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
        return testimonialRepository.save(t);
    }

    public void delete(Long id) {
        testimonialRepository.deleteById(id);
    }
}
