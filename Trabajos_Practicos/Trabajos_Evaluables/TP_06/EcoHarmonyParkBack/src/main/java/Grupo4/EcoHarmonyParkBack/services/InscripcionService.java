package Grupo4.EcoHarmonyParkBack.services;

import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.entities.Actividad;
import Grupo4.EcoHarmonyParkBack.entities.HorarioActividad;
import Grupo4.EcoHarmonyParkBack.entities.Inscripcion;
import Grupo4.EcoHarmonyParkBack.repositories.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class InscripcionService {
    private final InscripcionRepository inscripcionRepository;

    public List<Inscripcion> obtenerInscripciones(){
        return inscripcionRepository.findAll();
    }

    public int inscribirActividad(InscripcionRequest request){
        return -1;
    }

}
