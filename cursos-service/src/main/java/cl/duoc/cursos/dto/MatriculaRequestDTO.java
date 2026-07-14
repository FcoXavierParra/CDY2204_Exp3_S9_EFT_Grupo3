package cl.duoc.cursos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Datos que el BFF envía para persistir una matrícula (mensaje consumido de la cola). */
@Data
public class MatriculaRequestDTO {

    @NotBlank(message = "El codigo del curso es obligatorio")
    private String cursoCodigo;

    private String cursoNombre;

    @NotBlank(message = "El email del estudiante es obligatorio")
    private String estudianteEmail;

    private String estudianteNombre;
}
