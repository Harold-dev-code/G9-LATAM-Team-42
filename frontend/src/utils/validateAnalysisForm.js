/**
 * Pure validation function for the 10-field energy analysis form.
 *
 * Accepts raw form state where numeric fields are strings (from input elements)
 * and toggle fields are booleans. Parses strings to numbers internally for
 * range checking.
 *
 * @param {Object} formData — raw form state
 * @param {string} formData.consumo_kwh — monthly consumption (50–2000)
 * @param {string} formData.tipo_inmueble — property type ("casa"|"oficina"|"apartamento"|"comercio")
 * @param {string} formData.personas_vivienda — people in household (1–10)
 * @param {string} formData.cantidad_equipos — connected devices (1–20)
 * @param {string} formData.horas_alto_consumo — peak hours (0–24)
 * @param {boolean} formData.uso_horario_pico — peak schedule usage (no validation)
 * @param {string} formData.antiguedad_inmueble — building age in years (2–31, optional)
 * @param {boolean} formData.tiene_aire_acondicionado — has AC (no validation)
 * @param {boolean} formData.tiene_calentador_electrico — has electric heater (no validation)
 * @param {boolean} formData.electrodomesticos_eficientes — efficient appliances (no validation)
 * @returns {Object} errors — map of fieldName → error message string. Empty object means valid.
 */
export function validateAnalysisForm(formData) {
  const errors = {};

  // consumo_kwh: required, 50–2000
  const consumo = Number(formData.consumo_kwh);
  if (
    formData.consumo_kwh === "" ||
    formData.consumo_kwh == null ||
    isNaN(consumo) ||
    consumo < 50 ||
    consumo > 2000
  ) {
    errors.consumo_kwh = "Ingresa un consumo entre 50 y 2000 kWh.";
  }

  // personas_vivienda: required, 1–10
  const personas = Number(formData.personas_vivienda);
  if (
    formData.personas_vivienda === "" ||
    formData.personas_vivienda == null ||
    isNaN(personas) ||
    personas < 1 ||
    personas > 10
  ) {
    errors.personas_vivienda = "Ingresa entre 1 y 10 personas.";
  }

  // cantidad_equipos: required, 1–20
  const equipos = Number(formData.cantidad_equipos);
  if (
    formData.cantidad_equipos === "" ||
    formData.cantidad_equipos == null ||
    isNaN(equipos) ||
    equipos < 1 ||
    equipos > 20
  ) {
    errors.cantidad_equipos = "Ingresa entre 1 y 20 equipos.";
  }

  // horas_alto_consumo: required, 0–24
  const horas = Number(formData.horas_alto_consumo);
  if (
    formData.horas_alto_consumo === "" ||
    formData.horas_alto_consumo == null ||
    isNaN(horas) ||
    horas < 0 ||
    horas > 24
  ) {
    errors.horas_alto_consumo = "Ingresa entre 0 y 24 horas.";
  }

  // tipo_inmueble: required, must be one of the valid options
  const tiposValidos = ["casa", "oficina", "apartamento", "comercio"];
  if (
    !formData.tipo_inmueble ||
    !tiposValidos.includes(formData.tipo_inmueble.toLowerCase())
  ) {
    errors.tipo_inmueble = "Selecciona un tipo de inmueble válido.";
  }

  // antiguedad_inmueble: optional, but if provided must be 2–31
  if (
    formData.antiguedad_inmueble != null &&
    formData.antiguedad_inmueble !== ""
  ) {
    const antiguedad = Number(formData.antiguedad_inmueble);
    if (isNaN(antiguedad) || antiguedad < 2 || antiguedad > 31) {
      errors.antiguedad_inmueble =
        "La antigüedad debe estar entre 2 y 31 años.";
    }
  }

  // Boolean fields: no validation needed
  // uso_horario_pico, tiene_aire_acondicionado,
  // tiene_calentador_electrico, electrodomesticos_eficientes

  return errors;
}
