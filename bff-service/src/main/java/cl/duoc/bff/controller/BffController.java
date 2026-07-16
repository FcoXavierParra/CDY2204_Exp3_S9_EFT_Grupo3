package cl.duoc.bff.controller;

import cl.duoc.bff.dto.InscripcionMensaje;
import cl.duoc.bff.service.ConsumerService;
import cl.duoc.bff.service.CursosClient;
import cl.duoc.bff.service.ProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * BFF (Backend for Frontend): API que consume el frontend. Orquesta las colas
 * RabbitMQ (publicar/consumir) y delega la persistencia en el cursos-service.
 */
@RestController
@RequestMapping("/api/bff")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "BFF", description = "Orquestacion de colas + proxy al cursos-service")
@SecurityRequirement(name = "bearerAuth")
public class BffController {

    private final ProducerService producerService;
    private final ConsumerService consumerService;
    private final CursosClient cursosClient;

    /** ENDPOINT PRODUCTOR: el estudiante solicita inscripción → se publica en la cola. */
    @Operation(summary = "Publicar inscripcion en la cola (ESTUDIANTE)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @PostMapping("/inscripciones/publicar")
    public ResponseEntity<Map<String, Object>> publicar(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal Jwt jwt) {
        String cursoCodigo = req.get("cursoCodigo");
        if (cursoCodigo == null || cursoCodigo.isBlank()) {
            throw new IllegalArgumentException("cursoCodigo es obligatorio");
        }
        InscripcionMensaje msg = InscripcionMensaje.builder()
                .cursoCodigo(cursoCodigo)
                .cursoNombre(req.getOrDefault("cursoNombre", ""))
                .estudianteEmail(emailDe(jwt))
                .estudianteNombre(jwt.getClaimAsString("name"))
                .fechaSolicitud(LocalDateTime.now().toString())
                .simularError("true".equalsIgnoreCase(req.getOrDefault("simularError", "false")))
                .build();
        producerService.publicar(msg);
        return ResponseEntity.accepted().body(Map.of(
                "mensaje", "Inscripcion enviada a la cola",
                "cursoCodigo", cursoCodigo,
                "estudiante", msg.getEstudianteEmail()));
    }

    /** ENDPOINT CONSUMIDOR: extrae un mensaje de la cola y lo persiste (vía cursos-service → Oracle). */
    @Operation(summary = "Consumir un mensaje de la cola y persistir la matricula")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @PostMapping("/inscripciones/consumir")
    public ResponseEntity<Map<String, Object>> consumir(@AuthenticationPrincipal Jwt jwt) {
        InscripcionMensaje msg = consumerService.consumirUno();
        if (msg == null) {
            return ResponseEntity.ok(Map.of("mensaje", "La cola esta vacia, no hay inscripciones por procesar"));
        }
        // Manejo de errores: mensaje marcado (o fallido) -> se deriva a la COLA 2 (DLQ)
        if (msg.isSimularError()) {
            producerService.enviarAError(msg);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "mensaje", "Mensaje con error: derivado a la cola de errores (inscripciones.error.queue)",
                    "cursoCodigo", msg.getCursoCodigo()));
        }
        // Enriquecer el nombre del curso (el mensaje de la cola solo trae el codigo)
        if (msg.getCursoNombre() == null || msg.getCursoNombre().isBlank()) {
            try {
                for (Object o : cursosClient.listarCursos(jwt.getTokenValue())) {
                    if (o instanceof Map<?, ?> c && msg.getCursoCodigo().equals(c.get("codigo"))) {
                        Object nombre = c.get("nombre");
                        if (nombre != null) msg.setCursoNombre(nombre.toString());
                        break;
                    }
                }
            } catch (Exception ignore) { /* si falla el lookup, se persiste solo con el codigo */ }
        }
        Map<String, Object> matricula = cursosClient.crearMatricula(msg, jwt.getTokenValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Mensaje consumido de la cola y matricula persistida en Oracle",
                "matricula", matricula));
    }

    @Operation(summary = "Listar cursos (proxy al cursos-service)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @GetMapping("/cursos")
    public ResponseEntity<List<Object>> cursos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cursosClient.listarCursos(jwt.getTokenValue()));
    }

    @Operation(summary = "Crear curso (proxy al cursos-service, solo INSTRUCTOR)")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/cursos")
    public ResponseEntity<Map<String, Object>> crearCurso(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cursosClient.crearCurso(body, jwt.getTokenValue()));
    }

    @Operation(summary = "Listar matriculas procesadas (proxy al cursos-service)")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    @GetMapping("/matriculas")
    public ResponseEntity<List<Object>> matriculas(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cursosClient.listarMatriculas(jwt.getTokenValue()));
    }

    /** El claim 'emails' de Azure B2C es una lista; tomamos el primero. */
    private String emailDe(Jwt jwt) {
        List<String> emails = jwt.getClaimAsStringList("emails");
        if (emails != null && !emails.isEmpty()) return emails.get(0);
        String sub = jwt.getClaimAsString("name");
        return sub != null ? sub : jwt.getSubject();
    }
}
