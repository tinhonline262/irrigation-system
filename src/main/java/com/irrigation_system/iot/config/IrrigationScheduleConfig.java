package com.irrigation_system.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class IrrigationScheduleConfig {

    @Bean(name = "wateringScheduleTaskScheduler")
    public ThreadPoolTaskScheduler wateringScheduleTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("watering-schedule-");
        scheduler.initialize();
        return scheduler;
    }
}
