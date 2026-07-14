package cl.duoc.bff.service;

import cl.duoc.bff.dto.InscripcionMensaje;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Cliente REST hacia el cursos-service (core). Propaga el token JWT del usuario
 * (Authorization: Bearer ...) para que el core también valide la seguridad.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CursosClient {

    private final RestClient cursosRestClient;

    /** Persiste una matrícula en el core a partir del mensaje consumido de la cola. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> crearMatricula(InscripcionMensaje msg, String bearerToken) {
        Map<String, Object> body = Map.of(
                "cursoCodigo", msg.getCursoCodigo(),
                "cursoNombre", msg.getCursoNombre() == null ? "" : msg.getCursoNombre(),
                "estudianteEmail", msg.getEstudianteEmail(),
                "estudianteNombre", msg.getEstudianteNombre() == null ? "" : msg.getEstudianteNombre());
        return cursosRestClient.post()
                .uri("/api/matriculas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearCurso(Map<String, Object> body, String bearerToken) {
        return cursosRestClient.post()
                .uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    public List<Object> listarCursos(String bearerToken) {
        return cursosRestClient.get()
                .uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .body(List.class);
    }

    @SuppressWarnings("unchecked")
    public List<Object> listarMatriculas(String bearerToken) {
        return cursosRestClient.get()
                .uri("/api/matriculas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .body(List.class);
    }
}
