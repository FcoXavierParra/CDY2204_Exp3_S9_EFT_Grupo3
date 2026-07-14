package cl.duoc.cursos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/** Datos de entrada para crear/actualizar un curso. */
@Data
public class CursoRequestDTO {

    @NotBlank(message = "El codigo del curso es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre del curso es obligatorio")
    private String nombre;

    @NotBlank(message = "El instructor es obligatorio")
    private String instructor;

    private String descripcion;

    @NotNull(message = "Los cupos son obligatorios")
    @Min(value = 1, message = "Los cupos deben ser al menos 1")
    private Integer cupos;
}
