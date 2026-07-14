package cl.duoc.cursos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Matrícula de un estudiante en un curso. La crea el consumidor del BFF cuando
 * procesa un mensaje de inscripción desde la cola RabbitMQ. Persistida en Oracle
 * Cloud (tabla MATRICULA).
 */
@Entity
@Table(name = "MATRICULA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matricula_seq")
    @SequenceGenerator(name = "matricula_seq", sequenceName = "SEQ_MATRICULA", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CURSO_CODIGO", nullable = false, length = 40)
    private String cursoCodigo;

    @Column(name = "CURSO_NOMBRE", length = 200)
    private String cursoNombre;

    @Column(name = "ESTUDIANTE_EMAIL", nullable = false, length = 150)
    private String estudianteEmail;

    @Column(name = "ESTUDIANTE_NOMBRE", length = 150)
    private String estudianteNombre;

    @Column(name = "FECHA_MATRICULA", nullable = false)
    private LocalDateTime fechaMatricula;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @PrePersist
    void prePersist() {
        if (fechaMatricula == null) fechaMatricula = LocalDateTime.now();
        if (estado == null) estado = "MATRICULADO";
    }
}
