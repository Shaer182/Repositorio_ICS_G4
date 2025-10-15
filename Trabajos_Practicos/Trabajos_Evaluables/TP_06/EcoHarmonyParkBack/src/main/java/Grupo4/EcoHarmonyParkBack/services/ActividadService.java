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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActividadService {
    private final ActividadRepository actividadRepository;
    private final HorarioActividadRepository horarioRepository;
    private final VisitanteRepository visitanteRepository;
    private final InscripcionRepository inscripcionRepository;

    public List<ActividadResponse> obtenerActividades(){
        return this.actividadRepository.findAll()
                .stream().map(new ActividadToActividadResponse()).toList();
    }

    public List<HorarioResponse> obtenerHorarios(Long actividadId) {
        Actividad actividad = this.obtenerActividadPorId(actividadId)
                .orElseThrow(() -> new InvalidParameterException("No se encontró la actividad."));

        List<HorarioActividad> horarios = this.horarioRepository.findByActividad(actividad);

        return horarios.stream()
                .map(new HorarioToHorarioResponse())
                .toList();
    }

    public Optional<Actividad> obtenerActividadPorId(Long id){
        return this.actividadRepository.findById(id);
    }

    @Transactional
    public InscripcionResponse inscribirActividad(InscripcionRequest request) {
        // Buscar el horario
        HorarioActividad horario = horarioRepository.findById(request.getHorarioActividadId())
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        Actividad actividad = horario.getActividad();
        boolean requiereTalla = actividad.isRequiereVestimenta();

        // Validar cupos disponibles
        int cantidadSolicitada = request.getCantidadPersonas();
        if (horario.getCuposDisponibles() < cantidadSolicitada) {
            throw new RuntimeException("No hay cupos suficientes para este horario. Cupos disponibles: "
                    + horario.getCuposDisponibles());
        }

        // Descontar los cupos
        horario.setCuposDisponibles(horario.getCuposDisponibles() - cantidadSolicitada);
        horarioRepository.save(horario);

        // Crear la inscripción
        Inscripcion inscripcion = Inscripcion.builder()
                .horarioActividad(horario)
                .cantidadPersonas(cantidadSolicitada)
                .fechaInscripcion(LocalDateTime.now())
                .build();

        List<Grupo> grupos = new ArrayList<>();

        // Procesar visitantes
        for (VisitanteRequest vr : request.getVisitantes()) {

            if (requiereTalla && (vr.getTallaVestimenta() == null || vr.getTallaVestimenta().isEmpty())) {
                throw new RuntimeException("La actividad requiere talla de vestimenta para todos los visitantes");
            }

            Visitante visitante = visitanteRepository.findByDni(vr.getDni())
                    .map(existente -> {
                        existente.setNombre(vr.getNombre());
                        existente.setEdad(vr.getEdad());
                        if (vr.getTallaVestimenta() != null) {
                            existente.setTallaVestimenta(vr.getTallaVestimenta());
                        }
                        return visitanteRepository.save(existente);
                    })
                    .orElseGet(() -> visitanteRepository.save(
                            Visitante.builder()
                                    .nombre(vr.getNombre())
                                    .dni(vr.getDni())
                                    .edad(vr.getEdad())
                                    .tallaVestimenta(vr.getTallaVestimenta())
                                    .build()
                    ));

            Grupo grupo = Grupo.builder()
                    .visitante(visitante)
                    .inscripcion(inscripcion)
                    .build();

            grupos.add(grupo);
        }

        inscripcion.setGrupos(grupos);
        inscripcionRepository.save(inscripcion);

        return new InscripcionToInscripcionResponse().apply(inscripcion);
    }
}