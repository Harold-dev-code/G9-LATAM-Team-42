import { describe, it, expect } from "vitest";
import { buildPayload } from "./buildPayload.js";

const EXPECTED_KEYS = [
  "consumo_kwh",
  "tipo_inmueble",
  "nombre_inmueble",
  "direccion_inmueble",
  "pais",
  "moneda",
  "periodo",
  "mes_facturado",
  "anio_facturado",
  "etiqueta",
  "personas_vivienda",
  "cantidad_equipos",
  "horas_alto_consumo",
  "uso_horario_pico",
  "antiguedad_inmueble",
  "tiene_aire_acondicionado",
  "tiene_calentador_electrico",
  "electrodomesticos_eficientes",
  "tarifa_kwh",
];

describe("buildPayload", () => {
  const validFormData = {
    consumo_kwh: "350",
    tipo_inmueble: "casa",
    nombre_inmueble: "Casa Principal",
    direccion_inmueble: "Calle 10 # 20-30",
    pais: "Colombia",
    moneda: "cop",
    periodo: "mensual",
    mes_facturado: "9",
    anio_facturado: "2026",
    etiqueta: "Residencial",
    personas_vivienda: "4",
    cantidad_equipos: "10",
    horas_alto_consumo: "6",
    uso_horario_pico: true,
    antiguedad_inmueble: "15",
    tiene_aire_acondicionado: false,
    tiene_calentador_electrico: true,
    electrodomesticos_eficientes: false,
    tarifa_kwh: "0.75",
  };

  it("returns an object with exactly 19 keys", () => {
    const payload = buildPayload(validFormData);
    expect(Object.keys(payload)).toHaveLength(19);
  });

  it("converts string numeric fields to Number types", () => {
    const payload = buildPayload(validFormData);
    expect(payload.consumo_kwh).toBe(350);
    expect(payload.personas_vivienda).toBe(4);
    expect(payload.cantidad_equipos).toBe(10);
    expect(payload.horas_alto_consumo).toBe(6);
    expect(payload.antiguedad_inmueble).toBe(15);
    expect(payload.mes_facturado).toBe(9);
    expect(payload.anio_facturado).toBe(2026);
    expect(payload.tarifa_kwh).toBe(0.75);
  });

  it("converts boolean toggles to integer 0 or 1", () => {
    const payload = buildPayload(validFormData);
    expect(payload.uso_horario_pico).toBe(1);
    expect(payload.tiene_aire_acondicionado).toBe(0);
    expect(payload.tiene_calentador_electrico).toBe(1);
    expect(payload.electrodomesticos_eficientes).toBe(0);
  });

  it("capitalizes tipo_inmueble", () => {
    expect(buildPayload({ ...validFormData, tipo_inmueble: "casa" }).tipo_inmueble).toBe("Casa");
    expect(buildPayload({ ...validFormData, tipo_inmueble: "oficina" }).tipo_inmueble).toBe("Oficina");
    expect(buildPayload({ ...validFormData, tipo_inmueble: "apartamento" }).tipo_inmueble).toBe("Apartamento");
    expect(buildPayload({ ...validFormData, tipo_inmueble: "comercio" }).tipo_inmueble).toBe("Comercio");
  });

  it("defaults antiguedad_inmueble to 10 when empty string", () => {
    const payload = buildPayload({ ...validFormData, antiguedad_inmueble: "" });
    expect(payload.antiguedad_inmueble).toBe(10);
  });

  it("produces the 19 expected keys matching the backend contract", () => {
    const payload = buildPayload(validFormData);
    expect(Object.keys(payload).sort()).toEqual([...EXPECTED_KEYS].sort());
  });

  it("normalizes inmueble, país, moneda, período y etiqueta", () => {
    const payload = buildPayload(validFormData);
    expect(payload.nombre_inmueble).toBe("Casa Principal");
    expect(payload.direccion_inmueble).toBe("Calle 10 # 20-30");
    expect(payload.pais).toBe("Colombia");
    expect(payload.moneda).toBe("COP");
    expect(payload.periodo).toBe("mensual");
    expect(payload.etiqueta).toBe("Residencial");
  });

  it("aplica defaults cuando faltan país, moneda, tarifa, dirección y etiqueta", () => {
    const payload = buildPayload({
      ...validFormData,
      pais: undefined,
      moneda: undefined,
      periodo: undefined,
      etiqueta: undefined,
      tarifa_kwh: "",
      direccion_inmueble: "",
      nombre_inmueble: undefined,
    });
    expect(payload.pais).toBe("Colombia");
    expect(payload.moneda).toBe("COP");
    expect(payload.periodo).toBe("mensual");
    expect(payload.etiqueta).toBe("Residencial");
    expect(payload.tarifa_kwh).toBe(0.75);
    expect(payload.direccion_inmueble).toBeNull();
    expect(payload.nombre_inmueble).toBe("");
  });

  it("infers etiqueta Comercial for oficina and comercio", () => {
    expect(buildPayload({ ...validFormData, tipo_inmueble: "oficina", etiqueta: undefined }).etiqueta).toBe("Comercial");
    expect(buildPayload({ ...validFormData, tipo_inmueble: "comercio", etiqueta: undefined }).etiqueta).toBe("Comercial");
  });

  it("all boolean-sourced fields are integers, not booleans", () => {
    const payload = buildPayload(validFormData);
    expect(typeof payload.uso_horario_pico).toBe("number");
    expect(typeof payload.tiene_aire_acondicionado).toBe("number");
    expect(typeof payload.tiene_calentador_electrico).toBe("number");
    expect(typeof payload.electrodomesticos_eficientes).toBe("number");
  });
});
