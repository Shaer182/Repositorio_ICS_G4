package Grupo4.EcoHarmonyParkBack.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadResponse {
    private Long id;
    private String nombre;
    private boolean requiereVestimenta;
    private int cupoMaximo;
    private String descripcion;
    private String terminosCondiciones;
    private int edadMinima;
}
