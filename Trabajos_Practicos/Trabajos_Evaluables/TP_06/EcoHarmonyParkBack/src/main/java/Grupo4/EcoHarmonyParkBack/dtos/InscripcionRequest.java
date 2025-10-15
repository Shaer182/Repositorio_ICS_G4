package Grupo4.EcoHarmonyParkBack.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionRequest {
    private List<VisitanteRequest> visitantes;
    private Long horarioActividadId;
    private int cantidadPersonas;
}
