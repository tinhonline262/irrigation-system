package com.irrigation_system.iot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "air_sensor_reading", schema = "irrigation_system_db")
public class AirSensorReading {
    @Id
    @Size(max = 36)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @NotNull
    @Column(name = "humidity_percent", nullable = false)
    private Float humidityPercent;

    @NotNull
    @Column(name = "temperature_celsius", nullable = false)
    private Float temperatureCelsius;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;


}