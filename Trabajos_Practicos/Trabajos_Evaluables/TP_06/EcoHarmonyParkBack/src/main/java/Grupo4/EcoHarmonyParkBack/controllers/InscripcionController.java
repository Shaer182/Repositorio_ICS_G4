package Grupo4.EcoHarmonyParkBack.controllers;

import Grupo4.EcoHarmonyParkBack.dtos.ActividadResponse;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionRequest;
import Grupo4.EcoHarmonyParkBack.dtos.InscripcionResponse;
import Grupo4.EcoHarmonyParkBack.services.EmailService;
import Grupo4.EcoHarmonyParkBack.services.InscripcionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inscripciones")
@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {
    private final InscripcionService inscripcionService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<InscripcionResponse>> obtenerInscripciones(){
        List<InscripcionResponse> inscripciones = inscripcionService.obtenerInscripciones();
        return ResponseEntity.ok(inscripciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscripcionResponse> obtenerInscripcionPorId(@PathVariable Long id) {
        InscripcionResponse inscripcion = inscripcionService.obtenerInscripcionPorId(id);
        return ResponseEntity.ok(inscripcion);
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribirActividad(@RequestBody InscripcionRequest request) {
        InscripcionResponse response = inscripcionService.inscribirActividad(request);
        emailService.enviarConfirmacionInscripcion(response.getEmail(), response);

        return ResponseEntity.ok(response);
    }
}
