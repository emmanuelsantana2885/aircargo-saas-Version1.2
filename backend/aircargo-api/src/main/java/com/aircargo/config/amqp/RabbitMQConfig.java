package com.aircargo.config.amqp;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_DOMAIN = "aircargo.domain";
    public static final String QUEUE_FLIGHT = "aircargo.flight";
    public static final String QUEUE_BOOKING = "aircargo.booking";
    public static final String QUEUE_WAREHOUSE = "aircargo.warehouse";
    public static final String QUEUE_MAWB = "aircargo.mawb";
    public static final String QUEUE_ULD = "aircargo.uld";
    public static final String QUEUE_LOAD_PLANNING = "aircargo.load-planning";
    public static final String QUEUE_NOTIFICATION = "aircargo.notification";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}
