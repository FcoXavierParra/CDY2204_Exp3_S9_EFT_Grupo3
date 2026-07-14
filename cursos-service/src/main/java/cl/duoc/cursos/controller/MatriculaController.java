package cl.duoc.cursos.controller;

import cl.duoc.cursos.dto.MatriculaRequestDTO;
import cl.duoc.cursos.model.Matricula;
import cl.duoc.cursos.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de matrículas. La creación la invoca el BFF cuando consume un mensaje
 * de inscripción desde la cola RabbitMQ y lo persiste en Oracle Cloud.
 */
@RestController
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
@Tag(name = "Matriculas", description = "Matriculas persistidas en Oracle desde la cola")
@SecurityRequirement(name = "bearerAuth")
public class MatriculaController {

    private final MatriculaService matriculaService;

    @Operation(summary = "Persistir matricula (invocado por el BFF al consumir de la cola)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<Matricula> crear(@Valid @RequestBody MatriculaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matriculaService.crear(dto));
    }

    @Operation(summary = "Listar matriculas persistidas (Oracle)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<Matricula>> listar() {
        return ResponseEntity.ok(matriculaService.listar());
    }
}
