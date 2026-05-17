package com.irrigation_system.iot.queue.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceControlProducer {

    private final RabbitTemplate rabbitTemplate;

    private static final String MQTT_EXCHANGE = "amq.topic";

    /**
     * Publishes a control command (ON/OFF) to the device via MQTT topic exchange.
     * The ESP8266 subscribes to "device.control.{chipId}" and expects plain text "ON" or "OFF".
     */
    public void sendControlCommand(String chipId, String command) {
        String routingKey = "device.control." + chipId;

        // Send as plain text (not JSON) because the ESP8266 expects raw "ON" or "OFF"
        MessageProperties props = new MessageProperties();
        props.setContentType("text/plain");
        Message message = new Message(command.getBytes(), props);

        rabbitTemplate.send(MQTT_EXCHANGE, routingKey, message);
        log.info("Sent control command '{}' to chipId {} on topic '{}'", command, chipId, routingKey);
    }
}
