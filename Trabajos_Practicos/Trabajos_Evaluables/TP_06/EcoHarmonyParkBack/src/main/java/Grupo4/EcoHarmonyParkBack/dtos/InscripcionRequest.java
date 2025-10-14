package Grupo4.EcoHarmonyParkBack.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionRequest {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private Long eventoId;
    private Boolean aceptaTyC;
}
