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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        // Validar email
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("El correo electrónico es obligatorio.");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!request.getEmail().matches(emailRegex)) {
            throw new RuntimeException("El correo electrónico ingresado no es válido.");
        }

        // Buscar el horario
        HorarioActividad horario = horarioRepository.findById(request.getHorarioActividadId())
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        Actividad actividad = horario.getActividad();
        boolean requiereTalla = actividad.isRequiereVestimenta();
        int edadMinima = actividad.getEdadMinima();

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
