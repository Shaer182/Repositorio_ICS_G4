package Grupo4.EcoHarmonyParkBack.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionResponse {
    private Long id;
    private LocalDateTime fechaInscripcion;
    private int cantidadPersonas;
    private String email;
    private List<VisitanteResponse> visitantes;
    private HorarioResponse horario;
}
