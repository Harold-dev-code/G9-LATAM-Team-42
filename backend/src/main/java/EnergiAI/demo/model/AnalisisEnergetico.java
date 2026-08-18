package EnergiAI.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "analisis_energetico")
public class AnalisisEnergetico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos de entrada
    private Double consumoKwh;
    private Integer usoHorarioPico;
    private Integer cantidadEquipos;
    private String tipoInmueble;
    private Double horasAltoConsumo;
    private Integer personasVivienda;
    private Integer antiguedadInmueble;
    private Integer tieneAireAcondicionado;
    private Integer tieneCalentadorElectrico;
    private Integer electrodomesticosEficientes;

    // Campos de resultado
    private String categoria;
    private Double probabilidad;
    private Double costoEstimadoMensual;
    private Double tarifaKwh;

    // Recomendaciones
//    @Column(columnDefinition = "TEXT")
    @Lob
    private String recomendaciones;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
