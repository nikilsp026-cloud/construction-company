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

    private final WebsiteSettingRepository websiteSettingRepository;

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
        Map<String, String> map = new LinkedHashMap<>();
        for (WebsiteSetting setting : websiteSettingRepository.findAll()) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }

    public void saveAll(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            WebsiteSetting setting = websiteSettingRepository
                    .findBySettingKey(entry.getKey())
                    .orElseGet(() -> {
                        WebsiteSetting ws = new WebsiteSetting();
                        ws.setSettingKey(entry.getKey());
                        return ws;
                    });
            setting.setSettingValue(entry.getValue());
            websiteSettingRepository.save(setting);
        }
    }

    public WebsiteSetting save(WebsiteSetting ws) {
        return websiteSettingRepository.save(ws);
    }
}
