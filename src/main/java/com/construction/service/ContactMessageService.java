package com.construction.service;

import com.construction.entity.ContactMessage;
import com.construction.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Transactional(readOnly = true)
    public Page<ContactMessage> findAll(Pageable p) {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc(p);
    }

    @Transactional(readOnly = true)
    public Page<ContactMessage> findUnread(Pageable p) {
        return contactMessageRepository.findByReadOrderByCreatedAtDesc(false, p);
    }

    @Transactional(readOnly = true)
    public Optional<ContactMessage> findById(Long id) {
        return contactMessageRepository.findById(id);
    }

    public ContactMessage save(ContactMessage m) {
        return contactMessageRepository.save(m);
    }

    public void markAsRead(Long id) {
        contactMessageRepository.findById(id).ifPresent(message -> {
            message.setRead(true);
            contactMessageRepository.save(message);
        });
    }

    public void delete(Long id) {
        contactMessageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return contactMessageRepository.countByReadFalse();
    }
}
