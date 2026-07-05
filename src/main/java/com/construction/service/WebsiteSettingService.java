package com.construction.service;

import com.construction.entity.WebsiteSetting;
import com.construction.repository.WebsiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class WebsiteSettingService {

    private static final int MAX_VALUE_LENGTH = 10_000;

    private final WebsiteSettingRepository websiteSettingRepository;

    // Settings are read on every public/admin page render but only ever
    // written from the admin settings form, so an explicitly-invalidated
    // cache avoids a DB round-trip per request without risking staleness.
    private volatile Map<String, String> cachedSettings;

    @Transactional(readOnly = true)
    public List<WebsiteSetting> findAll() {
        return websiteSettingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public String getValue(String key) {
        return websiteSettingRepository.findBySettingKey(key)
                .map(WebsiteSetting::getSettingValue)
                .orElse("");
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllAsMap() {
        Map<String, String> cached = cachedSettings;
        if (cached != null) {
            return cached;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (WebsiteSetting setting : websiteSettingRepository.findAll()) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        cachedSettings = map;
        return map;
    }

    // Only updates settings that already exist - the form only ever renders
    // inputs for existing keys, so anything else is an unexpected/injected
    // parameter (e.g. Spring Security's own "_csrf" field) rather than a new
    // setting to create.
    public void saveAll(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            websiteSettingRepository.findBySettingKey(entry.getKey()).ifPresent(setting -> {
                String value = entry.getValue();
                if (value != null && value.length() > MAX_VALUE_LENGTH) {
                    value = value.substring(0, MAX_VALUE_LENGTH);
                }
                setting.setSettingValue(value);
                websiteSettingRepository.save(setting);
            });
        }
        cachedSettings = null;
    }

    public WebsiteSetting save(WebsiteSetting ws) {
        WebsiteSetting saved = websiteSettingRepository.save(ws);
        cachedSettings = null;
        return saved;
    }
}
