package Grupo4.EcoHarmonyParkBack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visitante_id")
    private Visitante visitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;
}