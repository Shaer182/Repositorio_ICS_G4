package Grupo4.EcoHarmonyParkBack.mappers;

import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.dtos.VisitanteResponse;
import Grupo4.EcoHarmonyParkBack.entities.Inscripcion;
import Grupo4.EcoHarmonyParkBack.entities.Visitante;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InscripcionToInscripcionResponse implements Function<Inscripcion, InscripcionResponse> {
    @Override
    public InscripcionResponse apply(Inscripcion inscripcion) {
        List<VisitanteResponse> visitantes = inscripcion.getGrupos().stream()
                .map(grupo -> {
                    Visitante v = grupo.getVisitante();
                    return VisitanteResponse.builder()
                            .id(v.getId())
                            .nombre(v.getNombre())
                            .edad(v.getEdad())
                            .dni(v.getDni())
                            .tallaVestimenta(v.getTallaVestimenta())
                            .build();
                })
                .collect(Collectors.toList());

        HorarioResponse horario = new HorarioToHorarioResponse().apply(inscripcion.getHorarioActividad());

        return InscripcionResponse.builder()
                .id(inscripcion.getId())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .cantidadPersonas(inscripcion.getCantidadPersonas())
                .visitantes(visitantes)
                .horario(horario)
                .email(inscripcion.getEmail())
                .build();
    }
}
