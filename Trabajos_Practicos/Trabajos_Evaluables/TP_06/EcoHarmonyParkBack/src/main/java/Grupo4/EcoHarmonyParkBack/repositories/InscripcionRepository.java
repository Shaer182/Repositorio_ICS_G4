package Grupo4.EcoHarmonyParkBack.repositories;

import Grupo4.EcoHarmonyParkBack.entities.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    @Query("""
        SELECT COUNT(i) > 0 
        FROM Inscripcion i 
        JOIN i.grupos g 
        WHERE i.horarioActividad.id = :horarioId 
        AND g.visitante.dni = :dni
    """)
    boolean existsByHorarioAndVisitanteDni(@Param("horarioId") Long horarioId, @Param("dni") String dni);
    
}
