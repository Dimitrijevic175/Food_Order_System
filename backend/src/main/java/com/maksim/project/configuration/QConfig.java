package com.maksim.project.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QConfig {

    // Definiši Queue
    @Bean
    public Queue orderStatusQueue() {
        return new Queue("orderStatusQueue", true);
    }

    @Bean
    public Queue orderDeliverQueue() {
        return new Queue("orderDeliverQueue", true);
    }
    @Bean
    public Queue orderDoneQueue() {
        return new Queue("orderDoneQueue", true);
    }

}
