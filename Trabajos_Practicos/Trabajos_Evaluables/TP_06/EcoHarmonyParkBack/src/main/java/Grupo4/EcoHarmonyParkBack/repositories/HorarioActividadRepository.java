package Grupo4.EcoHarmonyParkBack.repositories;

import Grupo4.EcoHarmonyParkBack.entities.Actividad;
import Grupo4.EcoHarmonyParkBack.entities.HorarioActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioActividadRepository extends JpaRepository<HorarioActividad, Long> {
    List<HorarioActividad> findByActividad(Actividad actividad);
}
