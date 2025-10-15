package Grupo4.EcoHarmonyParkBack.repositories;

import Grupo4.EcoHarmonyParkBack.entities.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {
    Optional<Visitante> findByDni(String dni);
}
