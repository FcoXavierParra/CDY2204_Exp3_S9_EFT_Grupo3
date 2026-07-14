package cl.duoc.cursos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Curso publicado por un instructor. Persistido en Oracle Cloud (tabla CURSO). */
@Entity
@Table(name = "CURSO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "curso_seq")
    @SequenceGenerator(name = "curso_seq", sequenceName = "SEQ_CURSO", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODIGO", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String nombre;

    @Column(name = "INSTRUCTOR", nullable = false, length = 150)
    private String instructor;

    @Column(name = "DESCRIPCION", length = 1000)
    private String descripcion;

    @Column(name = "CUPOS", nullable = false)
    private Integer cupos;

    /** Key del material del curso (PDF) dentro del bucket S3, si se subió. */
    @Column(name = "MATERIAL_S3KEY", length = 500)
    private String materialS3Key;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }
}
