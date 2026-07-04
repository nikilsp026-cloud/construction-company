package com.construction.service;

import com.construction.entity.ConstructionService;
import com.construction.repository.ConstructionServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConstructionServiceService {

    private final ConstructionServiceRepository constructionServiceRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ConstructionService> findAll() {
        return constructionServiceRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<ConstructionService> findActive() {
        return constructionServiceRepository.findByStatusOrderByDisplayOrderAsc(ConstructionService.ServiceStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Optional<ConstructionService> findById(Long id) {
        return constructionServiceRepository.findById(id);
    }

    public ConstructionService save(ConstructionService s) {
        return constructionServiceRepository.save(s);
    }

    public void delete(Long id) {
        constructionServiceRepository.findById(id).ifPresent(service -> {
            if (service.getImage() != null) {
                fileStorageService.deleteFile(service.getImage());
            }
            constructionServiceRepository.delete(service);
        });
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return constructionServiceRepository.countByStatus(ConstructionService.ServiceStatus.ACTIVE);
    }
}
