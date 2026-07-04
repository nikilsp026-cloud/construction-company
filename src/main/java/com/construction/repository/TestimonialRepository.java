package com.construction.repository;

import com.construction.entity.Testimonial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    List<Testimonial> findByStatusOrderByCreatedAtDesc(Testimonial.TestimonialStatus status);

    Page<Testimonial> findByStatus(Testimonial.TestimonialStatus status, Pageable pageable);

    long countByStatus(Testimonial.TestimonialStatus status);
}
