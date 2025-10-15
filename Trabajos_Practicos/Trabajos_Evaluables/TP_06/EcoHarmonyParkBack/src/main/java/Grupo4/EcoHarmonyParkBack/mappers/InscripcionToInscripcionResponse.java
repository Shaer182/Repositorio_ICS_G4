package Grupo4.EcoHarmonyParkBack.mappers;

import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.entities.Inscripcion;

import java.util.function.Function;

public class InscripcionToInscripcionResponse implements Function<Inscripcion, InscripcionResponse> {
    @Override
    public InscripcionResponse apply(Inscripcion inscripcion) {
        return InscripcionResponse.builder()
                .id(inscripcion.getId())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .cantidadPersonas(inscripcion.getCantidadPersonas())
                .build();
    }
}
