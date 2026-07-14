package cl.duoc.cursos.controller;

import cl.duoc.cursos.dto.CursoRequestDTO;
import cl.duoc.cursos.dto.CursoResponseDTO;
import cl.duoc.cursos.model.Curso;
import cl.duoc.cursos.service.CursoService;
import cl.duoc.cursos.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Endpoints de cursos. INSTRUCTOR administra (crear/editar/eliminar/subir material);
 * ESTUDIANTE e INSTRUCTOR pueden listar y descargar material.
 */
@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cursos", description = "Gestion de cursos + material en AWS S3")
@SecurityRequirement(name = "bearerAuth")
public class CursoController {

    private final CursoService cursoService;
    private final S3Service s3Service;

    @Operation(summary = "Listar cursos (ESTUDIANTE / INSTRUCTOR)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<CursoResponseDTO>> listar() {
        return ResponseEntity.ok(cursoService.listar().stream().map(CursoResponseDTO::fromEntity).toList());
    }

    @Operation(summary = "Obtener curso por id")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(CursoResponseDTO.fromEntity(cursoService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear curso (solo INSTRUCTOR)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<CursoResponseDTO> crear(@Valid @RequestBody CursoRequestDTO dto) {
        Curso c = cursoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(CursoResponseDTO.fromEntity(c));
    }

    @Operation(summary = "Actualizar curso (solo INSTRUCTOR)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CursoRequestDTO dto) {
        return ResponseEntity.ok(CursoResponseDTO.fromEntity(cursoService.actualizar(id, dto)));
    }

    @Operation(summary = "Eliminar curso (solo INSTRUCTOR)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Subir material del curso a AWS S3 (solo INSTRUCTOR)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/material")
    public ResponseEntity<Map<String, Object>> subirMaterial(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) throws IOException {
        Curso curso = cursoService.obtenerPorId(id);
        String key = s3Service.subirMaterial(curso.getCodigo(), archivo);
        Curso actualizado = cursoService.registrarMaterial(id, key);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Material subido a S3",
                "cursoId", id,
                "materialS3Key", actualizado.getMaterialS3Key()));
    }

    @Operation(summary = "Descargar material del curso desde S3 (ESTUDIANTE / INSTRUCTOR)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @GetMapping("/{id}/material")
    public ResponseEntity<byte[]> descargarMaterial(@PathVariable Long id) {
        Curso curso = cursoService.obtenerPorId(id);
        if (curso.getMaterialS3Key() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] contenido = s3Service.descargarPorKey(curso.getMaterialS3Key());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("material_" + curso.getCodigo()).build());
        return ResponseEntity.ok().headers(headers).body(contenido);
    }
}
