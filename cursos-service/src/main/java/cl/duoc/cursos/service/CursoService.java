package cl.duoc.cursos.service;

import cl.duoc.cursos.dto.CursoRequestDTO;
import cl.duoc.cursos.exception.ResourceNotFoundException;
import cl.duoc.cursos.model.Curso;
import cl.duoc.cursos.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CursoService {

    private final CursoRepository repository;

    @Transactional(readOnly = true)
    public List<Curso> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Curso obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Curso obtenerPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con codigo: " + codigo));
    }

    @Transactional
    public Curso crear(CursoRequestDTO dto) {
        if (repository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un curso con el codigo: " + dto.getCodigo());
        }
        Curso curso = Curso.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .instructor(dto.getInstructor())
                .descripcion(dto.getDescripcion())
                .cupos(dto.getCupos())
                .build();
        Curso guardado = repository.save(curso);
        log.info("Curso creado id={} codigo={}", guardado.getId(), guardado.getCodigo());
        return guardado;
    }

    @Transactional
    public Curso actualizar(Long id, CursoRequestDTO dto) {
        Curso curso = obtenerPorId(id);
        curso.setCodigo(dto.getCodigo());
        curso.setNombre(dto.getNombre());
        curso.setInstructor(dto.getInstructor());
        curso.setDescripcion(dto.getDescripcion());
        curso.setCupos(dto.getCupos());
        return repository.save(curso);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtenerPorId(id));
        log.info("Curso eliminado id={}", id);
    }

    @Transactional
    public Curso registrarMaterial(Long id, String s3Key) {
        Curso curso = obtenerPorId(id);
        curso.setMaterialS3Key(s3Key);
        return repository.save(curso);
    }
}
