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
@Table(name = "watering_schedule", schema = "irrigation_system_db")
public class WateringSchedule {
    @Id
    @Size(max = 36)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Size(max = 100)
    @NotNull
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @NotNull
    @Column(name = "water_amount_ml", nullable = false)
    private Float waterAmountMl;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "next_run_at")
    private Instant nextRunAt;


}