package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.SystemConfigDto;
import com.irrigation_system.iot.entity.SystemConfig;
import com.irrigation_system.iot.repository.SystemConfigRepository;
import com.irrigation_system.iot.service.SystemConfigService;
import com.irrigation_system.iot.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AuditLogService auditLogService;
    private static final String MQTT_EXCHANGE = "amq.topic";

    @Override
    public List<SystemConfigDto> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigDto updateConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findById(key)
                .orElseGet(() -> SystemConfig.builder().key(key).build());
        config.setValue(value);
        config = systemConfigRepository.save(config);

        if ("sample-rate".equals(key)) {
            // Push sample rate to devices over MQTT
            publishSampleRateLine(value);
        }

        auditLogService.logAction("CONFIG_UPDATE", key, java.util.Map.of("value", value));

        return toDto(config);
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getValue)
                .orElse(defaultValue);
    }

    @Override
    public byte[] exportBackup() {
        // Simplified backup export simulating JSON formatting for existing config
        List<SystemConfigDto> configs = getAllConfigs();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < configs.size(); i++) {
            SystemConfigDto c = configs.get(i);
            sb.append(String.format("  {\"key\": \"%s\", \"value\": \"%s\"}", c.getKey(), c.getValue().replace("\"", "\\\"")));
            if (i < configs.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
        }
        sb.append("]");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void publishSampleRateLine(String sampleRate) {
        try {
            // Sample rate routing key assumption or broadcasting to all devices
            String routingKey = "device.config.sample-rate";
            rabbitTemplate.convertAndSend(MQTT_EXCHANGE, routingKey, sampleRate);
            log.info("Published sample-rate {} to MQTT", sampleRate);
        } catch (Exception e) {
            log.error("Failed to publish sample-rate to MQTT", e);
        }
    }

    private SystemConfigDto toDto(SystemConfig entity) {
        if (entity == null) return null;
        return SystemConfigDto.builder()
                .key(entity.getKey())
                .value(entity.getValue())
                .updatedAt(entity.getLastModifiedAt() != null ? entity.getLastModifiedAt() : entity.getCreatedAt())
                .build();
    }
}
