package cl.duoc.bff.service;

import cl.duoc.bff.dto.InscripcionMensaje;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * PRODUCTOR: publica los mensajes de inscripción en la cola RabbitMQ.
 * (Endpoint "publicar" del BFF que orquesta la cola.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;
    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;
    @Value("${app.rabbitmq.dlx}")
    private String dlx;
    @Value("${app.rabbitmq.error-routingkey}")
    private String errorRoutingKey;

    public void publicar(InscripcionMensaje mensaje) {
        log.info("[PRODUCTOR] Publicando inscripcion en la cola: curso={} estudiante={}",
                mensaje.getCursoCodigo(), mensaje.getEstudianteEmail());
        rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);
    }

    /** Deriva el mensaje a la COLA 2 de errores (DLQ) cuando su procesamiento falla. */
    public void enviarAError(InscripcionMensaje mensaje) {
        log.warn("[DLQ] Derivando inscripcion a la cola de errores: curso={} estudiante={}",
                mensaje.getCursoCodigo(), mensaje.getEstudianteEmail());
        rabbitTemplate.convertAndSend(dlx, errorRoutingKey, mensaje);
    }
}
