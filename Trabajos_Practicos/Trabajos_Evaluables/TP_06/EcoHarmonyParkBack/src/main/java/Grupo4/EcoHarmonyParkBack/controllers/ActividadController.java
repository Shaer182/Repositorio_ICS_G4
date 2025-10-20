package Grupo4.EcoHarmonyParkBack.controllers;

import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.entities.Actividad;
import Grupo4.EcoHarmonyParkBack.mappers.ActividadToActividadResponse;
import Grupo4.EcoHarmonyParkBack.services.ActividadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "Actividades")
@RestController
@RequestMapping("/actividades")
@RequiredArgsConstructor
public class ActividadController {
    private final ActividadService actividadService;

    @GetMapping
    public ResponseEntity<List<ActividadResponse>> obtenerActividades(){
        List<ActividadResponse> actividades = actividadService.obtenerActividades();
        return ResponseEntity.ok(actividades);
    }

    @GetMapping("/{actividadId}")
    public ResponseEntity<ActividadResponse> obtenerActividadPorId(@PathVariable("actividadId") Long actividadId){
        Actividad actividad = actividadService.obtenerActividadPorId(actividadId)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada"));

        return ResponseEntity.ok(new ActividadToActividadResponse().apply(actividad));
    }

    @GetMapping("/{actividadId}/horarios")
    public ResponseEntity<List<HorarioResponse>> obtenerHorarios(
            @PathVariable("actividadId") Long actividadId,
            @RequestParam("fecha") LocalDate fecha
    ){
        List<HorarioResponse> horarios = actividadService.obtenerHorarios(actividadId, fecha);
        return ResponseEntity.ok(horarios);
    }
}
