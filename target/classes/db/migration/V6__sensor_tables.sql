-- ================================================
-- V6__sensor_tables.sql
-- Cảm biến độ ẩm đất & độ ẩm không khí + nhiệt độ
-- ================================================

-- Cảm biến độ ẩm đất
CREATE TABLE soil_sensor_reading
(
    id               VARCHAR(36) PRIMARY KEY,
    device_id        VARCHAR(36) NOT NULL,
    moisture_percent FLOAT       NOT NULL,   -- 0.0 → 100.0 %
    recorded_at      TIMESTAMP   NOT NULL,
    CONSTRAINT fk_soil_device
        FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE
);

CREATE INDEX idx_soil_device_time ON soil_sensor_reading (device_id, recorded_at DESC);

-- Cảm biến độ ẩm không khí + nhiệt độ
CREATE TABLE air_sensor_reading
(
    id                  VARCHAR(36) PRIMARY KEY,
    device_id           VARCHAR(36) NOT NULL,
    humidity_percent    FLOAT       NOT NULL,   -- 0.0 → 100.0 %
    temperature_celsius FLOAT       NOT NULL,   -- độ C
    recorded_at         TIMESTAMP   NOT NULL,
    CONSTRAINT fk_air_device
        FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE
);

CREATE INDEX idx_air_device_time ON air_sensor_reading (device_id, recorded_at DESC);