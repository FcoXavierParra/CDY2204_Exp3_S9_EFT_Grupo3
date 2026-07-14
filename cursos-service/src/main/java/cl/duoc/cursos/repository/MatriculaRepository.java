package cl.duoc.cursos.repository;

import cl.duoc.cursos.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    List<Matricula> findByEstudianteEmail(String estudianteEmail);
    List<Matricula> findByCursoCodigo(String cursoCodigo);
}
