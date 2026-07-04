package com.construction.repository;

import com.construction.entity.WebsiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebsiteSettingRepository extends JpaRepository<WebsiteSetting, Long> {

    Optional<WebsiteSetting> findBySettingKey(String settingKey);

    boolean existsBySettingKey(String settingKey);
}
