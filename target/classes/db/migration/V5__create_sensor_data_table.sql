CREATE TABLE sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    temperature DOUBLE,
    humidity DOUBLE,
    soil_moisture DOUBLE,
    timestamp DATETIME NOT NULL
);
