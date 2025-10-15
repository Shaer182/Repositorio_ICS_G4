package Grupo4.EcoHarmonyParkBack.controllers;

import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.dtos.HorarioResponse;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.mappers.ActividadToActividadResponse;
import Grupo4.EcoHarmonyParkBack.services.ActividadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{actividadId}/horarios")
    public ResponseEntity<List<HorarioResponse>> obtenerHorarios(@PathVariable("actividadId") Long actividadId){
        List<HorarioResponse> horarios = actividadService.obtenerHorarios(actividadId);
        return ResponseEntity.ok(horarios);
    }

    @PostMapping("/inscripciones")
    public ResponseEntity<InscripcionResponse> inscribirActividad(@RequestBody InscripcionRequest request) {
        InscripcionResponse response = actividadService.inscribirActividad(request);
        return ResponseEntity.ok(response);
    }
}
