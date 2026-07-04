package com.construction.repository;

import com.construction.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ContactMessage> findByReadOrderByCreatedAtDesc(boolean read, Pageable pageable);

    /** Badge count for the admin sidebar */
    long countByReadFalse();
}
