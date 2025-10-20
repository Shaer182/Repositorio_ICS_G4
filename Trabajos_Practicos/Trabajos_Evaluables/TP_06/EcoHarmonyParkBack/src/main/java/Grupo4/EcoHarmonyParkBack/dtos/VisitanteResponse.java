package Grupo4.EcoHarmonyParkBack.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitanteResponse {
    private Long id;
    private String nombre;
    private String dni;
    private int edad;
    private String tallaVestimenta;
}
