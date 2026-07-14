package cl.duoc.bff.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infraestructura RabbitMQ del BFF: cola de inscripciones + cola de errores (DLQ).
 * El BFF produce mensajes (endpoint publicar) y los consume (endpoint consumir).
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;
    @Value("${app.rabbitmq.queue}")
    private String queue;
    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;
    @Value("${app.rabbitmq.dlx}")
    private String dlx;
    @Value("${app.rabbitmq.error-queue}")
    private String errorQueue;
    @Value("${app.rabbitmq.error-routingkey}")
    private String errorRoutingKey;

    @Bean
    public DirectExchange inscripcionesExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange inscripcionesDlx() {
        return new DirectExchange(dlx, true, false);
    }

    @Bean
    public Queue inscripcionesQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", errorRoutingKey)
                .build();
    }

    @Bean
    public Queue inscripcionesErrorQueue() {
        return QueueBuilder.durable(errorQueue).build();
    }

    @Bean
    public Binding inscripcionesBinding(Queue inscripcionesQueue, DirectExchange inscripcionesExchange) {
        return BindingBuilder.bind(inscripcionesQueue).to(inscripcionesExchange).with(routingKey);
    }

    @Bean
    public Binding inscripcionesErrorBinding(Queue inscripcionesErrorQueue, DirectExchange inscripcionesDlx) {
        return BindingBuilder.bind(inscripcionesErrorQueue).to(inscripcionesDlx).with(errorRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter jsonMessageConverter) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(jsonMessageConverter);
        return t;
    }
}
