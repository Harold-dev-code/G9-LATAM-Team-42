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
@Table(name = "analisis_energetico", uniqueConstraints = {
    @UniqueConstraint(name = "uk_analisis_inmueble_periodo",
        columnNames = {"inmueble_id", "mes_facturado", "anio_facturado"})
})
public class AnalisisEnergetico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inmueble_id", nullable = false)
    private Inmueble inmueble;

    @Column(nullable = false)
    private Integer mesFacturado;

    @Column(nullable = false)
    private Integer anioFacturado;

    // Campos de entrada
    private Double consumoKwh;
    private Integer usoHorarioPico;
    private Integer cantidadEquipos;
    private String tipoInmueble;
    private String pais;
    private String moneda;
    private String periodo;
    private String etiqueta;
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

    // Relación con usuario
    private Long usuarioId;

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
