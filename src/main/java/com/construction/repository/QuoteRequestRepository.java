package com.construction.repository;

import com.construction.entity.QuoteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    Page<QuoteRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<QuoteRequest> findByStatusOrderByCreatedAtDesc(QuoteRequest.QuoteStatus status, Pageable pageable);

    long countByStatus(QuoteRequest.QuoteStatus status);
}
