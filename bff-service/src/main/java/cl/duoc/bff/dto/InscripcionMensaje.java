package cl.duoc.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mensaje de inscripción que viaja por la cola RabbitMQ (JSON). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionMensaje {
    private String cursoCodigo;
    private String cursoNombre;
    private String estudianteEmail;
    private String estudianteNombre;
    private String fechaSolicitud;
    /** Bandera de demostración: si es true, el consumidor deriva el mensaje a la cola de errores (DLQ). */
    private boolean simularError;
}
