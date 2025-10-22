package Grupo4.EcoHarmonyParkBack.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitanteRequest {
    @NotBlank(message = "El nombre del visitante es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El DNI del visitante es obligatorio.")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener entre 7 y 8 dígitos numéricos.")
    private String dni;

    @Min(value = 1, message = "La edad del visitante debe ser mayor que 0.")
    private int edad;

    private String tallaVestimenta;
}
