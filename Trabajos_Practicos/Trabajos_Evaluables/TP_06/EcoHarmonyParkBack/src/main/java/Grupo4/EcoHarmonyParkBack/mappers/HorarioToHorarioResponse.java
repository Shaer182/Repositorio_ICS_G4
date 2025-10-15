package Grupo4.EcoHarmonyParkBack.mappers;

import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.entities.HorarioActividad;

import java.util.function.Function;

public class HorarioToHorarioResponse implements Function<HorarioActividad, HorarioResponse> {
    @Override
    public HorarioResponse apply(HorarioActividad horarioActividad) {
        return HorarioResponse.builder()
                .id(horarioActividad.getId())
                .horaInicio(horarioActividad.getHoraInicio())
                .horaFin(horarioActividad.getHoraFin())
                .build();
    }
}
