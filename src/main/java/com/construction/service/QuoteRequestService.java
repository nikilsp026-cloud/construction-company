package com.construction.service;

import com.construction.entity.QuoteRequest;
import com.construction.repository.QuoteRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteRequestService {

    private final QuoteRequestRepository quoteRequestRepository;

    @Transactional(readOnly = true)
    public Page<QuoteRequest> findAll(Pageable p) {
        return quoteRequestRepository.findAllByOrderByCreatedAtDesc(p);
    }

    @Transactional(readOnly = true)
    public Page<QuoteRequest> findByStatus(QuoteRequest.QuoteStatus s, Pageable p) {
        return quoteRequestRepository.findByStatusOrderByCreatedAtDesc(s, p);
    }

    @Transactional(readOnly = true)
    public Optional<QuoteRequest> findById(Long id) {
        return quoteRequestRepository.findById(id);
    }

    public QuoteRequest save(QuoteRequest q) {
        return quoteRequestRepository.save(q);
    }

    public void updateStatus(Long id, QuoteRequest.QuoteStatus newStatus) {
        quoteRequestRepository.findById(id).ifPresent(quote -> {
            quote.setStatus(newStatus);
            quoteRequestRepository.save(quote);
        });
    }

    public void delete(Long id) {
        quoteRequestRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countByStatus(QuoteRequest.QuoteStatus s) {
        return quoteRequestRepository.countByStatus(s);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statusSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        Arrays.stream(QuoteRequest.QuoteStatus.values())
                .forEach(status -> summary.put(status.name(), quoteRequestRepository.countByStatus(status)));
        return summary;
    }
}
