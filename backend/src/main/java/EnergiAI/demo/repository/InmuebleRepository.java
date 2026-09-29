package EnergiAI.demo.repository;

import EnergiAI.demo.model.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {

    @Query("select i from Inmueble i where i.usuario.id = :usuarioId and lower(i.nombre) = lower(:nombre)")
    Optional<Inmueble> findByUsuarioAndNombre(@Param("usuarioId") Long usuarioId,
                                               @Param("nombre") String nombre);
}
