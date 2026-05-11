package com.irrigation_system.iot.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    public static final String SENSOR_DATA_QUEUE = "sensor.data.queue";
    public static final String DEVICE_REGISTER_QUEUE = "device.register.queue";
    public static final String DEVICE_STATUS_QUEUE = "device.status.queue";

    @Bean
    public Queue sensorDataQueue() {
        return new Queue(SENSOR_DATA_QUEUE, true);
    }

    @Bean
    public Queue deviceRegisterQueue() {
        return new Queue(DEVICE_REGISTER_QUEUE, true);
    }

    @Bean
    public Queue deviceStatusQueue() {
        return new Queue(DEVICE_STATUS_QUEUE, true);
    }

    @Bean
    public TopicExchange mqttTopicExchange() {
        return new TopicExchange("amq.topic");
    }

    @Bean
    public Binding sensorMqttBinding(Queue sensorDataQueue, TopicExchange mqttTopicExchange) {
        return BindingBuilder.bind(sensorDataQueue).to(mqttTopicExchange).with("sensor.data.#");
    }

    @Bean
    public Binding registerMqttBinding(Queue deviceRegisterQueue, TopicExchange mqttTopicExchange) {
        return BindingBuilder.bind(deviceRegisterQueue).to(mqttTopicExchange).with("device.register");
    }

    @Bean
    public Binding statusMqttBinding(Queue deviceStatusQueue, TopicExchange mqttTopicExchange) {
        return BindingBuilder.bind(deviceStatusQueue).to(mqttTopicExchange).with("device.status.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Batch factory — used by SensorDataConsumer (List<SensorDataDTO>).
     * Default name kept so Spring Boot auto-wires it as the primary listener factory.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setBatchListener(true);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(200);
        return factory;
    }

    /**
     * Non-batch factory — used by DeviceMqttConsumer (single-message listeners).
     * Explicitly disables batch mode to override any property-driven batch settings
     * applied by the configurer (e.g. spring.rabbitmq.listener.simple.consumer-batch-enabled).
     */
    @Bean
    public SimpleRabbitListenerContainerFactory singleRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setBatchListener(false);
        factory.setConsumerBatchEnabled(false);
        return factory;
    }
}
