package com.construction.repository;

import com.construction.entity.ConstructionService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructionServiceRepository extends JpaRepository<ConstructionService, Long> {

    List<ConstructionService> findByStatusOrderByDisplayOrderAsc(ConstructionService.ServiceStatus status);

    List<ConstructionService> findAllByOrderByDisplayOrderAsc();

    long countByStatus(ConstructionService.ServiceStatus status);
}
