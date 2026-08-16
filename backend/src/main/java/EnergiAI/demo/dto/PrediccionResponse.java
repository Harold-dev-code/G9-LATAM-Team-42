package EnergiAI.demo.dto;

import java.util.List;

public class PrediccionResponse {
    private String categoria;
    private Double probabilidad;
    private List<String> recomendaciones;

    // No-args constructor for Jackson deserialization
    public PrediccionResponse() {
    }

    public PrediccionResponse(String categoria, Double probabilidad) {
        this.categoria = categoria;
        this.probabilidad = probabilidad;
    }

    public PrediccionResponse(String categoria, Double probabilidad, List<String> recomendaciones) {
        this.categoria = categoria;
        this.probabilidad = probabilidad;
        this.recomendaciones = recomendaciones;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Double getProbabilidad() { return probabilidad; }
    public void setProbabilidad(Double probabilidad) { this.probabilidad = probabilidad; }

    public List<String> getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(List<String> recomendaciones) { this.recomendaciones = recomendaciones; }
}
