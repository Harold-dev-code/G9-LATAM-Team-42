/**
 * Converts UI form state into the API-contract payload object.
 * All type coercions happen here (boolean → 0/1, string → Number, etc.)
 *
 * @param {Object} formData — validated form state from AnalysisForm
 * @param {string} formData.consumo_kwh — numeric string (50–2000)
 * @param {string} formData.tipo_inmueble — lowercase: "casa" | "oficina" | "apartamento" | "comercio"
 * @param {string} formData.personas_vivienda — numeric string (1–10)
 * @param {string} formData.cantidad_equipos — numeric string (1–20)
 * @param {string} formData.horas_alto_consumo — numeric string (0–24)
 * @param {boolean} formData.uso_horario_pico — toggle state
 * @param {string} formData.antiguedad_inmueble — numeric string (2–31) or "" for default
 * @param {boolean} formData.tiene_aire_acondicionado — toggle state
 * @param {boolean} formData.tiene_calentador_electrico — toggle state
 * @param {boolean} formData.electrodomesticos_eficientes — toggle state
 * @returns {Object} payload — 10-field object matching POST /analisis-energetico contract
 */
export function buildPayload(formData) {
  return {
    consumo_kwh: Number(formData.consumo_kwh),
    tipo_inmueble: capitalize(formData.tipo_inmueble),
    personas_vivienda: Number(formData.personas_vivienda),
    cantidad_equipos: Number(formData.cantidad_equipos),
    horas_alto_consumo: Number(formData.horas_alto_consumo),
    uso_horario_pico: formData.uso_horario_pico ? 1 : 0,
    antiguedad_inmueble:
      formData.antiguedad_inmueble === "" ? 10 : Number(formData.antiguedad_inmueble),
    tiene_aire_acondicionado: formData.tiene_aire_acondicionado ? 1 : 0,
    tiene_calentador_electrico: formData.tiene_calentador_electrico ? 1 : 0,
    electrodomesticos_eficientes: formData.electrodomesticos_eficientes ? 1 : 0,
    tarifa_kwh: formData.tarifa_kwh === "" ? 0.75 : Number(formData.tarifa_kwh),
  };
}

/**
 * Capitalizes the first letter of a string.
 * @param {string} str
 * @returns {string}
 */
function capitalize(str) {
  if (!str) return str;
  return str.charAt(0).toUpperCase() + str.slice(1);
}
