package Grupo4.EcoHarmonyParkBack.mappers;

import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.entities.Actividad;

import java.util.function.Function;

public class ActividadToActividadResponse implements Function<Actividad, ActividadResponse> {
    @Override
    public ActividadResponse apply(Actividad actividad) {
        return ActividadResponse.builder()
                .id(actividad.getId())
                .nombre(actividad.getNombre())
                .descripcion(actividad.getDescripcion())
                .cupoMaximo(actividad.getCupoMaximo())
                .requiereVestimenta(actividad.isRequiereVestimenta())
                .terminosCondiciones(actividad.getTerminosCondiciones())
                .edadMinima(actividad.getEdadMinima())
                .build();
    }
}
