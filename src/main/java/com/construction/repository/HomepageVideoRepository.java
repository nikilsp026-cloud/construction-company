package com.construction.repository;

import com.construction.entity.HomepageVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomepageVideoRepository extends JpaRepository<HomepageVideo, Long> {

    List<HomepageVideo> findByStatusOrderByCreatedAtDesc(HomepageVideo.VideoStatus status);
}
