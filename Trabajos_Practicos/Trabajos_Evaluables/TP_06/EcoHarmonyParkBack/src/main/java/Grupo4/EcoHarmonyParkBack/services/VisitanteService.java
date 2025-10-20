package Grupo4.EcoHarmonyParkBack.services;

import Grupo4.EcoHarmonyParkBack.dtos.VisitanteRequest;
import Grupo4.EcoHarmonyParkBack.entities.Visitante;
import Grupo4.EcoHarmonyParkBack.repositories.VisitanteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisitanteService {
    private final VisitanteRepository visitanteRepository;

    @Transactional
    public Visitante crearVisitante(VisitanteRequest request){
        return visitanteRepository.findByDni(request.getDni())
                .map(existente -> {
                    existente.setNombre(request.getNombre());
                    existente.setEdad(request.getEdad());
                    if (request.getTallaVestimenta() != null) {
                        existente.setTallaVestimenta(request.getTallaVestimenta());
                    }
                    return visitanteRepository.save(existente);
                })
                .orElseGet(() -> visitanteRepository.save(
                        Visitante.builder()
                                .nombre(request.getNombre())
                                .dni(request.getDni())
                                .edad(request.getEdad())
                                .tallaVestimenta(request.getTallaVestimenta())
                                .build()
                ));
    }
}
