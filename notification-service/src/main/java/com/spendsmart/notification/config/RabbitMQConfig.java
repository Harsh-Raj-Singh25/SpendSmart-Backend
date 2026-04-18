package com.spendsmart.notification.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
	public static final String QUEUE_NAME="notification.expense.queue";
	public static final String EXCHANGE_NAME = "spendsmart.exchange";
    public static final String ROUTING_KEY = "expense.created";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "spendsmart.dlx";
    public static final String DEAD_LETTER_QUEUE_NAME = "notification.expense.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "notification.expense.dlq";
    
    @Bean
    public Queue expenseQueue() {
	    	return QueueBuilder.durable(QUEUE_NAME)
	    			.withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME)
	    			.withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
	    			.build();
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
	    return new DirectExchange(DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    public Queue deadLetterQueue() {
	    return QueueBuilder.durable(DEAD_LETTER_QUEUE_NAME).build();
    }
    
    @Bean 
    public Binding binding(Queue expenseQueue, TopicExchange exchange) {
    	return BindingBuilder.bind(expenseQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
	    return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
    	return new Jackson2JsonMessageConverter();
    }
}
