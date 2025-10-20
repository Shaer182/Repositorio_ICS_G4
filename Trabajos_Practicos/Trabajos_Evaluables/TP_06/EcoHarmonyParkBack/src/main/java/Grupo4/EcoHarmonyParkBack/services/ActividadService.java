package Grupo4.EcoHarmonyParkBack.services;

import Grupo4.EcoHarmonyParkBack.dtos.*;
import Grupo4.EcoHarmonyParkBack.entities.*;
import Grupo4.EcoHarmonyParkBack.mappers.ActividadToActividadResponse;
import Grupo4.EcoHarmonyParkBack.mappers.HorarioToHorarioResponse;
import Grupo4.EcoHarmonyParkBack.mappers.InscripcionToInscripcionResponse;
import Grupo4.EcoHarmonyParkBack.repositories.ActividadRepository;
import Grupo4.EcoHarmonyParkBack.repositories.HorarioActividadRepository;
import Grupo4.EcoHarmonyParkBack.repositories.InscripcionRepository;
import Grupo4.EcoHarmonyParkBack.repositories.VisitanteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActividadService {
    private final ActividadRepository actividadRepository;
    private final HorarioActividadRepository horarioRepository;

    public List<ActividadResponse> obtenerActividades(){
        return this.actividadRepository.findAll()
                .stream().map(new ActividadToActividadResponse()).toList();
    }

    public List<HorarioResponse> obtenerHorarios(Long actividadId, LocalDate fecha) {
        if (fecha == null) {
            throw new InvalidParameterException("La fecha es obligatoria.");
        }

        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // Validar que la fecha no sea anterior a hoy
        if (fecha.isBefore(hoy)) {
            throw new InvalidParameterException("La fecha debe ser igual o posterior a la actual.");
        }

        // Buscar la actividad
        Actividad actividad = this.obtenerActividadPorId(actividadId)
                .orElseThrow(() -> new InvalidParameterException("No se encontró la actividad."));

        // Obtener horarios de esa fecha
        List<HorarioActividad> horarios = horarioRepository.findByActividadAndFecha(actividad, fecha);

        // Si la fecha es hoy, filtrar horarios que aún no comenzaron
        if (fecha.isEqual(hoy)) {
            horarios = horarios.stream()
                    .filter(h -> h.getHoraInicio().isAfter(horaActual) || h.getHoraInicio().equals(horaActual))
                    .toList();
        }

        return horarios.stream()
                .map(new HorarioToHorarioResponse())
                .toList();
    }

    public Optional<Actividad> obtenerActividadPorId(Long id){
        return this.actividadRepository.findById(id);
    }
}