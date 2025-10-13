package Grupo4.EcoHarmonyParkBack.controllers;

import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.mappers.ActividadToActividadResponse;
import Grupo4.EcoHarmonyParkBack.services.ActividadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Actividades")
@RestController
@RequestMapping("/actividades")
@RequiredArgsConstructor
public class ActividadController {
    private final ActividadService actividadService;

    @GetMapping
    public ResponseEntity<List<ActividadResponse>> obtenerActividades(){
        List<ActividadResponse> actividades = actividadService.obtenerActividades()
                .stream().map(new ActividadToActividadResponse()).toList();

        return ResponseEntity.ok(actividades);
    }
}
