package com.irrigation_system.iot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "device", schema = "irrigation_system_db")
public class Device {
    @Id
    @Size(max = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Version
    @NotNull
    @ColumnDefault("0")
    @Column(name = "ol", nullable = false)
    private Long ol;

    @Column(name = "created_at")
    private Instant createdAt;

    @Size(max = 255)
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @Size(max = 255)
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Size(max = 50)
    @Column(name = "chip_id", unique = true)
    private String chipId;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'offline'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "owner_id", length = 36)
    private String ownerId;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "status_relay", nullable = false)
    private Boolean statusRelay = false;

    @Column(name = "moisture_threshold_low")
    private Float moistureThresholdLow;

    @Column(name = "moisture_threshold_high")
    private Float moistureThresholdHigh;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "auto_water_enabled", nullable = false)
    private Boolean autoWaterEnabled;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @ColumnDefault("0.0")
    @Column(name = "soil_moisture_offset")
    private Float soilMoistureOffset = 0.0f;

    @ColumnDefault("0.0")
    @Column(name = "air_temperature_offset")
    private Float airTemperatureOffset = 0.0f;

    @ColumnDefault("0.0")
    @Column(name = "air_humidity_offset")
    private Float airHumidityOffset = 0.0f;

    @Column(name = "wifi_rssi")
    private Integer wifiRssi;

    @Column(name = "ip")
    private String ip;

    @Column(name = "free_heap")
    private Long freeHeap;

    @Column(name = "uptime")
    private Long uptime;

}