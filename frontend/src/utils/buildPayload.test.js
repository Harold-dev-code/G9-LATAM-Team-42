import { describe, it, expect } from "vitest";
import { buildPayload } from "./buildPayload.js";

describe("buildPayload", () => {
  const validFormData = {
    consumo_kwh: "350",
    tipo_inmueble: "casa",
    personas_vivienda: "4",
    cantidad_equipos: "10",
    horas_alto_consumo: "6",
    uso_horario_pico: true,
    antiguedad_inmueble: "15",
    tiene_aire_acondicionado: false,
    tiene_calentador_electrico: true,
    electrodomesticos_eficientes: false,
  };

  it("returns an object with exactly 10 keys", () => {
    const payload = buildPayload(validFormData);
    expect(Object.keys(payload)).toHaveLength(10);
  });

  it("converts string numeric fields to Number types", () => {
    const payload = buildPayload(validFormData);
    expect(payload.consumo_kwh).toBe(350);
    expect(payload.personas_vivienda).toBe(4);
    expect(payload.cantidad_equipos).toBe(10);
    expect(payload.horas_alto_consumo).toBe(6);
    expect(payload.antiguedad_inmueble).toBe(15);
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

  it("produces only the 10 expected keys matching the backend contract", () => {
    const payload = buildPayload(validFormData);
    const expectedKeys = [
      "consumo_kwh",
      "tipo_inmueble",
      "personas_vivienda",
      "cantidad_equipos",
      "horas_alto_consumo",
      "uso_horario_pico",
      "antiguedad_inmueble",
      "tiene_aire_acondicionado",
      "tiene_calentador_electrico",
      "electrodomesticos_eficientes",
    ];
    expect(Object.keys(payload).sort()).toEqual(expectedKeys.sort());
  });

  it("all boolean-sourced fields are integers, not booleans", () => {
    const payload = buildPayload(validFormData);
    expect(typeof payload.uso_horario_pico).toBe("number");
    expect(typeof payload.tiene_aire_acondicionado).toBe("number");
    expect(typeof payload.tiene_calentador_electrico).toBe("number");
    expect(typeof payload.electrodomesticos_eficientes).toBe("number");
  });
});
