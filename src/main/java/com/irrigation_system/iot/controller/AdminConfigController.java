package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.SystemConfigDto;
import com.irrigation_system.iot.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<List<SystemConfigDto>> getAllConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllConfigs());
    }

    @PutMapping("/retention")
    public ResponseEntity<SystemConfigDto> updateRetention(@RequestBody Map<String, String> body) {
        String value = body.get("value");
        return ResponseEntity.ok(systemConfigService.updateConfig("retention", value));
    }

    @PutMapping("/sample-rate")
    public ResponseEntity<SystemConfigDto> updateSampleRate(@RequestBody Map<String, String> body) {
        String value = body.get("value");
        return ResponseEntity.ok(systemConfigService.updateConfig("sample-rate", value));
    }

    @PutMapping("/timezone")
    public ResponseEntity<SystemConfigDto> updateTimezone(@RequestBody Map<String, String> body) {
        String value = body.get("value");
        return ResponseEntity.ok(systemConfigService.updateConfig("timezone", value));
    }

    @GetMapping("/backup")
    public ResponseEntity<Resource> downloadBackup() {
        byte[] data = systemConfigService.exportBackup();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=config-backup.json")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(data.length)
                .body(resource);
    }
}
