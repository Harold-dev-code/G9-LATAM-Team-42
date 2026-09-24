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

    @NotBlank(message = "El país es obligatorio")
    @Schema(description = "País del análisis para personalizar la conversión y la tarifa local", example = "Colombia")
    private String pais;

    @NotBlank(message = "La moneda es obligatoria")
    @Schema(description = "Moneda local asociada al país", example = "COP")
    private String moneda;

    @NotBlank(message = "El período es obligatorio")
    @Schema(description = "Período del análisis", example = "mensual")
    private String periodo;

    @NotBlank(message = "La etiqueta es obligatoria")
    @Pattern(regexp = "Residencial|Comercial", message = "La etiqueta debe ser: Residencial o Comercial")
    @Schema(description = "Etiqueta de uso del inmueble", example = "Residencial", allowableValues = {"Residencial", "Comercial"})
    private String etiqueta;

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

    @Schema(description = "Tarifa del kWh en USD que aplica el usuario", example = "0.75")
    private Double tarifa_kwh;

    @NotBlank(message = "El nombre del inmueble es obligatorio")
    private String nombre_inmueble;

    private String direccion_inmueble;

    @NotNull(message = "El mes facturado es obligatorio")
    @Min(value = 1, message = "El mes facturado debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes facturado debe estar entre 1 y 12")
    private Integer mes_facturado;

    @NotNull(message = "El año facturado es obligatorio")
    @Min(value = 2000, message = "El año facturado no es válido")
    @Max(value = 2100, message = "El año facturado no es válido")
    private Integer anio_facturado;

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
        this.pais = "Colombia";
        this.moneda = "COP";
        this.periodo = "mensual";
        this.etiqueta = "Residencial";
        this.personas_vivienda = personas_vivienda;
        this.cantidad_equipos = cantidad_equipos;
        this.horas_alto_consumo = horas_alto_consumo;
        this.uso_horario_pico = uso_horario_pico;
        this.antiguedad_inmueble = antiguedad_inmueble;
        this.tiene_aire_acondicionado = tiene_aire_acondicionado;
        this.tiene_calentador_electrico = tiene_calentador_electrico;
        this.electrodomesticos_eficientes = electrodomesticos_eficientes;
    }

    public AnalisisRequest(Double consumo_kwh, String tipo_inmueble, String pais, String moneda, String periodo, String etiqueta,
                           Integer personas_vivienda, Integer cantidad_equipos, Double horas_alto_consumo,
                           Integer uso_horario_pico, Integer antiguedad_inmueble,
                           Integer tiene_aire_acondicionado, Integer tiene_calentador_electrico,
                           Integer electrodomesticos_eficientes, Double tarifa_kwh) {
        this.consumo_kwh = consumo_kwh;
        this.tipo_inmueble = tipo_inmueble;
        this.pais = pais;
        this.moneda = moneda;
        this.periodo = periodo;
        this.etiqueta = etiqueta;
        this.personas_vivienda = personas_vivienda;
        this.cantidad_equipos = cantidad_equipos;
        this.horas_alto_consumo = horas_alto_consumo;
        this.uso_horario_pico = uso_horario_pico;
        this.antiguedad_inmueble = antiguedad_inmueble;
        this.tiene_aire_acondicionado = tiene_aire_acondicionado;
        this.tiene_calentador_electrico = tiene_calentador_electrico;
        this.electrodomesticos_eficientes = electrodomesticos_eficientes;
        this.tarifa_kwh = tarifa_kwh;
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

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
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

    public Double getTarifa_kwh() {
        return tarifa_kwh;
    }

    public void setTarifa_kwh(Double tarifa_kwh) {
        this.tarifa_kwh = tarifa_kwh;
    }

    public String getNombre_inmueble() {
        return nombre_inmueble;
    }

    public void setNombre_inmueble(String nombre_inmueble) {
        this.nombre_inmueble = nombre_inmueble;
    }

    public String getDireccion_inmueble() {
        return direccion_inmueble;
    }

    public void setDireccion_inmueble(String direccion_inmueble) {
        this.direccion_inmueble = direccion_inmueble;
    }

    public Integer getMes_facturado() {
        return mes_facturado;
    }

    public void setMes_facturado(Integer mes_facturado) {
        this.mes_facturado = mes_facturado;
    }

    public Integer getAnio_facturado() {
        return anio_facturado;
    }

    public void setAnio_facturado(Integer anio_facturado) {
        this.anio_facturado = anio_facturado;
    }
}
