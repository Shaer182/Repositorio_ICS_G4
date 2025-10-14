package Grupo4.EcoHarmonyParkBack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "visitantes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visitante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "dni")
    private String dni;

    @Column(name = "edad")
    private int edad;

    @Column(name = "talla_vestimenta")
    private String tallaVestimenta;

    @OneToMany(mappedBy = "visitante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grupo> grupos;

    public Visitante(String nombre, String dni, int edad, String tallaVestimenta) {
        this.nombre = nombre;
        this.dni = dni;
        this.edad = edad;
        this.tallaVestimenta = tallaVestimenta;
    }
}
