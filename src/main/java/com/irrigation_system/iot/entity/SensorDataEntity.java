package com.irrigation_system.iot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
public class SensorDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private String deviceId;

    private Double temperature;
    private Double humidity;

    @Column(name = "soil_moisture")
    private Double soilMoisture;

    private LocalDateTime timestamp;
}
