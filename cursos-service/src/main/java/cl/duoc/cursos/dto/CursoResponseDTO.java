package cl.duoc.cursos.dto;

import cl.duoc.cursos.model.Curso;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CursoResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String instructor;
    private String descripcion;
    private Integer cupos;
    private String materialS3Key;
    private LocalDateTime fechaCreacion;

    public static CursoResponseDTO fromEntity(Curso c) {
        return CursoResponseDTO.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .nombre(c.getNombre())
                .instructor(c.getInstructor())
                .descripcion(c.getDescripcion())
                .cupos(c.getCupos())
                .materialS3Key(c.getMaterialS3Key())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
