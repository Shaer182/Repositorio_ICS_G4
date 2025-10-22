package Grupo4.EcoHarmonyParkBack.services;

import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.dtos.VisitanteRequest;
import Grupo4.EcoHarmonyParkBack.entities.*;
import Grupo4.EcoHarmonyParkBack.mappers.InscripcionToInscripcionResponse;
import Grupo4.EcoHarmonyParkBack.repositories.HorarioActividadRepository;
import Grupo4.EcoHarmonyParkBack.repositories.InscripcionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InscripcionService {
    private final InscripcionRepository inscripcionRepository;
    private final HorarioActividadRepository horarioRepository;
    private final VisitanteService visitanteService;

    public List<InscripcionResponse> obtenerInscripciones(){
        return inscripcionRepository.findAll().stream()
                .map(new InscripcionToInscripcionResponse())
                .toList();
    }

    public InscripcionResponse obtenerInscripcionPorId(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la inscripción con id: " + id));

        return new InscripcionToInscripcionResponse().apply(inscripcion);
    }


    @Transactional
    public InscripcionResponse inscribirActividad(InscripcionRequest request) {
        // Buscar el horario
        HorarioActividad horario = horarioRepository.findById(request.getHorarioActividadId())
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        Actividad actividad = horario.getActividad();
        boolean requiereTalla = actividad.isRequiereVestimenta();
        int edadMinima = actividad.getEdadMinima();

        // Validar coherencia entre cantidad y lista de visitantes
        if (request.getCantidadPersonas() != request.getVisitantes().size()) {
            throw new RuntimeException("La cantidad de personas no coincide con la cantidad de visitantes ingresados.");
        }

        // Validar cupos disponibles
        int cantidadSolicitada = request.getCantidadPersonas();
        if (horario.getCuposDisponibles() < cantidadSolicitada) {
            throw new RuntimeException("No hay cupos suficientes para este horario. Cupos disponibles: "
                    + horario.getCuposDisponibles());
        }

        // Validar fecha y hora del horario
        if (horario.getFecha().isBefore(LocalDate.now()) ||
                (horario.getFecha().isEqual(LocalDate.now()) && horario.getHoraInicio().isBefore(LocalTime.now()))) {
            throw new RuntimeException("No se puede inscribir a un horario que ya ha pasado.");
        }

        // Evitar duplicados dentro del mismo request
        Set<String> dnisUnicos = new HashSet<>();
        for (VisitanteRequest vr : request.getVisitantes()) {
            if (!dnisUnicos.add(vr.getDni())) {
                throw new RuntimeException("El visitante con DNI " + vr.getDni() + " está duplicado en la inscripción.");
            }
        }

        // Descontar los cupos
        horario.setCuposDisponibles(horario.getCuposDisponibles() - cantidadSolicitada);
        horarioRepository.save(horario);

        // Crear la inscripción
        Inscripcion inscripcion = Inscripcion.builder()
                .horarioActividad(horario)
                .cantidadPersonas(cantidadSolicitada)
                .fechaInscripcion(LocalDateTime.now())
                .email(request.getEmail())
                .build();

        List<Grupo> grupos = new ArrayList<>();

        // Procesar visitantes
        for (VisitanteRequest vr : request.getVisitantes()) {

            if (requiereTalla && (vr.getTallaVestimenta() == null || vr.getTallaVestimenta().isEmpty())) {
                throw new RuntimeException("La actividad requiere talla de vestimenta para todos los visitantes");
            }

            if (vr.getEdad() < edadMinima) {
                throw new RuntimeException("El visitante " + vr.getNombre() + " tiene " + vr.getEdad() + " años, y la edad mínima requerida es " + edadMinima + " años.");
            }

            // Validar si el visitante ya está inscripto en el mismo horario
            if (inscripcionRepository.existsByHorarioAndVisitanteDni(horario.getId(), vr.getDni())) {
                throw new RuntimeException("El visitante con DNI " + vr.getDni()
                        + " ya está inscripto en este horario.");
            }

            Visitante visitante = visitanteService.crearVisitante(vr);

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
