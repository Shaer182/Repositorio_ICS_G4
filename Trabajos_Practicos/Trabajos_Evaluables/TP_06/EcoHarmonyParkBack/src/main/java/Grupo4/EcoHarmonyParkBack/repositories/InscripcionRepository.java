package Grupo4.EcoHarmonyParkBack.repositories;

import Grupo4.EcoHarmonyParkBack.entities.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

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

    @Query("""
            SELECT COUNT(i) > 0 FROM Inscripcion i 
            JOIN i.grupos g JOIN i.horarioActividad h
            WHERE g.visitante.dni = :dni AND h.fecha = :fecha 
            AND h.id != :horarioIdExcluir
            AND ( (h.horaInicio < :horaFin AND h.horaFin > :horaInicio))
    """)
    boolean existsConflictingScheduleForVisitor(
            @Param("dni") String dni,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("horarioIdExcluir") Long horarioIdExcluir);
}
