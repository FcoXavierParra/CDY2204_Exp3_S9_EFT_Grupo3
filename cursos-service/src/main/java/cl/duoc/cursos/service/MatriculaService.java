package cl.duoc.cursos.service;

import cl.duoc.cursos.dto.MatriculaRequestDTO;
import cl.duoc.cursos.model.Matricula;
import cl.duoc.cursos.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatriculaService {

    private final MatriculaRepository repository;

    /** Persiste una matrícula (llamado por el BFF al consumir un mensaje de la cola). */
    @Transactional
    public Matricula crear(MatriculaRequestDTO dto) {
        Matricula m = Matricula.builder()
                .cursoCodigo(dto.getCursoCodigo())
                .cursoNombre(dto.getCursoNombre())
                .estudianteEmail(dto.getEstudianteEmail())
                .estudianteNombre(dto.getEstudianteNombre())
                .fechaMatricula(LocalDateTime.now())
                .estado("MATRICULADO")
                .build();
        Matricula guardada = repository.save(m);
        log.info("Matricula persistida en Oracle id={} curso={} estudiante={}",
                guardada.getId(), guardada.getCursoCodigo(), guardada.getEstudianteEmail());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Matricula> listar() {
        return repository.findAll();
    }
}
