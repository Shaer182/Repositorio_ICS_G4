package Grupo4.EcoHarmonyParkBack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "actividades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "requiere_vestimenta")
    private boolean requiereVestimenta;

    @Column(name = "cupo_maximo")
    private int cupoMaximo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "terminos_condiciones")
    private String terminosCondiciones;

    @Column(name = "edad_minima")
    private int edadMinima;

    // Relación con Horarios
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioActividad> horarios;
}
