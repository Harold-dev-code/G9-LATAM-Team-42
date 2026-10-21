package EnergiAI.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO de salida con los resultados del análisis energético.
 */

@Schema(description = "Resultado del análosis energético generado por el sistema")
public class AnalisisResponse {

    @Schema(description = "Categoría asignada según el puntaje de eficiencia", example = "Ineficiente")
    private String categoria;

    @Schema(description = "Probabilidad calculada por el modelo (entre 0 y 1)", example = "0.87")
    private Double probabilidad; // Valor decimal continuo entre 0 y 1 para el porcentaje

    @Schema(description = "Lista de sugerencias para mejorar la eficiencia energética",
            example = "[\"Reducir el uso de equipos durante los horarios pico\"]")
    private List<String> recomendaciones; // Lista de sugerencias para el usuario

    @Schema(description = "Costo económico estimado en base al consumo", example = "337.87")
    private Double costo_estimado; // Costo económico proyectado
    private Double costo_estimado_usd;
    private Double costo_estimado_local;
    private String pais;
    private String moneda;
    private Double huella_carbono_kg;
    private Double factor_emision_kg_co2_kwh;
    private Integer anio_factor_emision;
    private String fuente_factor_emision;

    // Constructor vacío
    public AnalisisResponse() {
    }

    // Constructor con parámetros
    public AnalisisResponse(String categoria, Double probabilidad, List<String> recomendaciones, Double costo_estimado) {
        this.categoria = categoria;
        this.probabilidad = probabilidad;
        this.recomendaciones = recomendaciones;
        this.costo_estimado = costo_estimado;
    }

    // Getters y Setters
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public List<String> getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(List<String> recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public Double getCosto_estimado() {
        return costo_estimado;
    }

    public void setCosto_estimado(Double costo_estimado) {
        this.costo_estimado = costo_estimado;
    }

    public Double getCosto_estimado_usd() {
        return costo_estimado_usd;
    }

    public void setCosto_estimado_usd(Double costo_estimado_usd) {
        this.costo_estimado_usd = costo_estimado_usd;
    }

    public Double getCosto_estimado_local() {
        return costo_estimado_local;
    }

    public void setCosto_estimado_local(Double costo_estimado_local) {
        this.costo_estimado_local = costo_estimado_local;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Double getHuella_carbono_kg() {
        return huella_carbono_kg;
    }

    public void setHuella_carbono_kg(Double huella_carbono_kg) {
        this.huella_carbono_kg = huella_carbono_kg;
    }

    public Double getFactor_emision_kg_co2_kwh() {
        return factor_emision_kg_co2_kwh;
    }

    public void setFactor_emision_kg_co2_kwh(Double factor_emision_kg_co2_kwh) {
        this.factor_emision_kg_co2_kwh = factor_emision_kg_co2_kwh;
    }

    public Integer getAnio_factor_emision() {
        return anio_factor_emision;
    }

    public void setAnio_factor_emision(Integer anio_factor_emision) {
        this.anio_factor_emision = anio_factor_emision;
    }

    public String getFuente_factor_emision() {
        return fuente_factor_emision;
    }

    public void setFuente_factor_emision(String fuente_factor_emision) {
        this.fuente_factor_emision = fuente_factor_emision;
    }
}