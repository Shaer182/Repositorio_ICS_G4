package Grupo4.EcoHarmonyParkBack.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscripcionRequest {
    @NotNull(message = "Debe especificar el horario de la actividad.")
    private Long horarioActividadId;

    @NotEmpty(message = "Debe incluir al menos un visitante.")
    @Valid // Aplica validaciones dentro de cada VisitanteRequest
    private List<VisitanteRequest> visitantes;

    @Positive(message = "La cantidad de personas debe ser mayor que cero.")
    private int cantidadPersonas;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    private String email;
}