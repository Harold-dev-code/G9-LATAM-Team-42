package EnergiAI.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para la evaluación de eficiencia energética.
 * Usa snake_case para los nombres de campo, coincidiendo con el contrato JSON.
 */
@Schema(description = "Objeto de transferencia de datos para solicitar una evaluación de eficiencia energética")
public class AnalisisRequest {

    // --- Campos obligatorios ---

    @NotNull(message = "El consumo en kWh es obligatorio")
    @Min(value = 50, message = "El consumo debe ser al menos 50 kWh")
    @Max(value = 2000, message = "El consumo no puede exceder 2000 kWh")
    @Schema(description = "Consumo mensual en Kilovatios-hora", example = "350.0")
    private Double consumo_kwh;

    @NotBlank(message = "El tipo de inmueble es obligatorio")
    @Pattern(regexp = "Casa|Oficina|Apartamento|Comercio", message = "El tipo de inmueble debe ser: Casa, Oficina, Apartamento o Comercio")
    @Schema(description = "Tipo de propiedad analizada", example = "Casa", allowableValues = {"Casa", "Oficina", "Apartamento", "Comercio"})
    private String tipo_inmueble;

    @NotNull(message = "La cantidad de personas en la vivienda es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 persona en la vivienda")
    @Max(value = 10, message = "El máximo de personas en la vivienda es 10")
    @Schema(description = "Número de personas que habitan la vivienda", example = "4")
    private Integer personas_vivienda;

    @NotNull(message = "La cantidad de equipos es obligatoria")
    @Min(value = 1, message = "La cantidad de equipos debe ser al menos 1")
    @Max(value = 20, message = "La cantidad de equipos no puede exceder 20")
    @Schema(description = "Cantidad total de electrodomésticos o equipos eléctricos", example = "8")
    private Integer cantidad_equipos;

    @NotNull(message = "Las horas de alto consumo son obligatorias")
    @DecimalMin(value = "0.0", message = "Las horas de alto consumo deben ser al menos 0.0")
    @DecimalMax(value = "24.0", message = "Las horas de alto consumo no pueden exceder 24.0")
    @Schema(description = "Horas promedio de alto consumo diario", example = "5.0")
    private Double horas_alto_consumo;

    // --- Campos opcionales ---

    @Schema(description = "Indica si el mayor uso se da en horas pico (0=No, 1=Sí)", example = "1")
    private Integer uso_horario_pico;

    @Schema(description = "Antigüedad del inmueble en años", example = "15")
    private Integer antiguedad_inmueble;

    @Schema(description = "Indica si tiene aire acondicionado (0=No, 1=Sí)", example = "0")
    private Integer tiene_aire_acondicionado;

    @Schema(description = "Indica si tiene calentador eléctrico (0=No, 1=Sí)", example = "1")
    private Integer tiene_calentador_electrico;

    @Schema(description = "Indica si los electrodomésticos son eficientes (0=No, 1=Sí)", example = "0")
    private Integer electrodomesticos_eficientes;

    // Constructor vacío
    public AnalisisRequest() {
    }

    // Constructor con todos los campos
    public AnalisisRequest(Double consumo_kwh, String tipo_inmueble, Integer personas_vivienda,
                           Integer cantidad_equipos, Double horas_alto_consumo,
                           Integer uso_horario_pico, Integer antiguedad_inmueble,
                           Integer tiene_aire_acondicionado, Integer tiene_calentador_electrico,
                           Integer electrodomesticos_eficientes) {
        this.consumo_kwh = consumo_kwh;
        this.tipo_inmueble = tipo_inmueble;
        this.personas_vivienda = personas_vivienda;
        this.cantidad_equipos = cantidad_equipos;
        this.horas_alto_consumo = horas_alto_consumo;
        this.uso_horario_pico = uso_horario_pico;
        this.antiguedad_inmueble = antiguedad_inmueble;
        this.tiene_aire_acondicionado = tiene_aire_acondicionado;
        this.tiene_calentador_electrico = tiene_calentador_electrico;
        this.electrodomesticos_eficientes = electrodomesticos_eficientes;
    }

    // Getters y Setters

    public Double getConsumo_kwh() {
        return consumo_kwh;
    }

    public void setConsumo_kwh(Double consumo_kwh) {
        this.consumo_kwh = consumo_kwh;
    }

    public String getTipo_inmueble() {
        return tipo_inmueble;
    }

    public void setTipo_inmueble(String tipo_inmueble) {
        this.tipo_inmueble = tipo_inmueble;
    }

    public Integer getPersonas_vivienda() {
        return personas_vivienda;
    }

    public void setPersonas_vivienda(Integer personas_vivienda) {
        this.personas_vivienda = personas_vivienda;
    }

    public Integer getCantidad_equipos() {
        return cantidad_equipos;
    }

    public void setCantidad_equipos(Integer cantidad_equipos) {
        this.cantidad_equipos = cantidad_equipos;
    }

    public Double getHoras_alto_consumo() {
        return horas_alto_consumo;
    }

    public void setHoras_alto_consumo(Double horas_alto_consumo) {
        this.horas_alto_consumo = horas_alto_consumo;
    }

    public Integer getUso_horario_pico() {
        return uso_horario_pico;
    }

    public void setUso_horario_pico(Integer uso_horario_pico) {
        this.uso_horario_pico = uso_horario_pico;
    }

    public Integer getAntiguedad_inmueble() {
        return antiguedad_inmueble;
    }

    public void setAntiguedad_inmueble(Integer antiguedad_inmueble) {
        this.antiguedad_inmueble = antiguedad_inmueble;
    }

    public Integer getTiene_aire_acondicionado() {
        return tiene_aire_acondicionado;
    }

    public void setTiene_aire_acondicionado(Integer tiene_aire_acondicionado) {
        this.tiene_aire_acondicionado = tiene_aire_acondicionado;
    }

    public Integer getTiene_calentador_electrico() {
        return tiene_calentador_electrico;
    }

    public void setTiene_calentador_electrico(Integer tiene_calentador_electrico) {
        this.tiene_calentador_electrico = tiene_calentador_electrico;
    }

    public Integer getElectrodomesticos_eficientes() {
        return electrodomesticos_eficientes;
    }

    public void setElectrodomesticos_eficientes(Integer electrodomesticos_eficientes) {
        this.electrodomesticos_eficientes = electrodomesticos_eficientes;
    }
}
