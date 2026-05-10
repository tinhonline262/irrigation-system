package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.DashboardSummaryDTO;
import com.irrigation_system.iot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket endpoint for realtime dashboard subscription
    @MessageMapping("/dashboard/{deviceId}")
    @SendTo("/topic/dashboard/{deviceId}")
    public DashboardSummaryDTO subscribeToDashboard(@DestinationVariable String deviceId) {
        return dashboardService.getDashboardSummary(deviceId);
    }
}
