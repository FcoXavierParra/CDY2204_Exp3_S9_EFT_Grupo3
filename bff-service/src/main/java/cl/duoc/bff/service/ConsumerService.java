package cl.duoc.bff.service;

import cl.duoc.bff.dto.InscripcionMensaje;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * CONSUMIDOR: extrae (pull) un mensaje de inscripción desde la cola RabbitMQ.
 * (Endpoint "consumir" del BFF.) Devuelve null si la cola está vacía.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    public InscripcionMensaje consumirUno() {
        Object obj = rabbitTemplate.receiveAndConvert(queue);
        if (obj == null) {
            log.info("[CONSUMIDOR] La cola '{}' esta vacia", queue);
            return null;
        }
        InscripcionMensaje mensaje = (InscripcionMensaje) obj;
        log.info("[CONSUMIDOR] Mensaje recibido de la cola: curso={} estudiante={}",
                mensaje.getCursoCodigo(), mensaje.getEstudianteEmail());
        return mensaje;
    }
}
