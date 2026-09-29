package EnergiAI.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO de respuesta para los elementos del historial de análisis")
public class AnalisisHistorialResponse {

    private Long id;
    private Integer mesFacturado;
    private Integer anioFacturado;
    private Double consumoKwh;
    private String categoria;
    private Double probabilidad;
    private Double costoEstimadoMensual;
    private Double tarifaKwh;
    private String recomendaciones;
    private LocalDateTime fechaCreacion;
    private String nombreInmueble;
    private String direccionInmueble;

    // Constructor vacío
    public AnalisisHistorialResponse() {}

    // Constructor completo para mapeo rápido
    public AnalisisHistorialResponse(Long id, Integer mesFacturado, Integer anioFacturado, 
                                     Double consumoKwh, String categoria, Double probabilidad, 
                                     Double costoEstimadoMensual, Double tarifaKwh, 
                                     String recomendaciones, LocalDateTime fechaCreacion, 
                                     String nombreInmueble, String direccionInmueble) {
        this.id = id;
        this.mesFacturado = mesFacturado;
        this.anioFacturado = anioFacturado;
        this.consumoKwh = consumoKwh;
        this.categoria = categoria;
        this.probabilidad = probabilidad;
        this.costoEstimadoMensual = costoEstimadoMensual;
        this.tarifaKwh = tarifaKwh;
        this.recomendaciones = recomendaciones;
        this.fechaCreacion = fechaCreacion;
        this.nombreInmueble = nombreInmueble;
        this.direccionInmueble = direccionInmueble;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMesFacturado() { return mesFacturado; }
    public void setMesFacturado(Integer mesFacturado) { this.mesFacturado = mesFacturado; }
    public Integer getAnioFacturado() { return anioFacturado; }
    public void setAnioFacturado(Integer anioFacturado) { this.anioFacturado = anioFacturado; }
    public Double getConsumoKwh() { return consumoKwh; }
    public void setConsumoKwh(Double consumoKwh) { this.consumoKwh = consumoKwh; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Double getProbabilidad() { return probabilidad; }
    public void setProbabilidad(Double probabilidad) { this.probabilidad = probabilidad; }
    public Double getCostoEstimadoMensual() { return costoEstimadoMensual; }
    public void setCostoEstimadoMensual(Double costoEstimadoMensual) { this.costoEstimadoMensual = costoEstimadoMensual; }
    public Double getTarifaKwh() { return tarifaKwh; }
    public void setTarifaKwh(Double tarifaKwh) { this.tarifaKwh = tarifaKwh; }
    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getNombreInmueble() { return nombreInmueble; }
    public void setNombreInmueble(String nombreInmueble) { this.nombreInmueble = nombreInmueble; }
    public String getDireccionInmueble() { return direccionInmueble; }
    public void setDireccionInmueble(String direccionInmueble) { this.direccionInmueble = direccionInmueble; }
}