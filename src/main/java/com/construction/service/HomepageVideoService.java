package com.construction.service;

import com.construction.entity.HomepageVideo;
import com.construction.entity.HomepageVideo.VideoStatus;
import com.construction.repository.HomepageVideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HomepageVideoService {

    private final HomepageVideoRepository homepageVideoRepository;

    public HomepageVideoService(HomepageVideoRepository homepageVideoRepository) {
        this.homepageVideoRepository = homepageVideoRepository;
    }

    @Transactional(readOnly = true)
    public List<HomepageVideo> findActive() {
        return homepageVideoRepository.findByStatusOrderByCreatedAtDesc(VideoStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Page<HomepageVideo> findAll(Pageable p) {
        return homepageVideoRepository.findAll(p);
    }

    @Transactional(readOnly = true)
    public Optional<HomepageVideo> findById(Long id) {
        return homepageVideoRepository.findById(id);
    }

    public HomepageVideo save(HomepageVideo v) {
        return homepageVideoRepository.save(v);
    }

    public void delete(Long id) {
        homepageVideoRepository.deleteById(id);
    }
}
