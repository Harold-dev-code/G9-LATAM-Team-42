import { describe, it, expect } from 'vitest';
import fc from 'fast-check';
import { buildPayload } from '../utils/buildPayload.js';

/**
 * Arbitrary that generates valid form data objects matching AnalysisForm state shape.
 * All required fields are within their valid ranges.
 */
function arbValidFormData() {
  return fc.record({
    consumo_kwh: fc.integer({ min: 50, max: 2000 }).map(String),
    tipo_inmueble: fc.constantFrom('casa', 'oficina', 'apartamento', 'comercio'),
    personas_vivienda: fc.integer({ min: 1, max: 10 }).map(String),
    cantidad_equipos: fc.integer({ min: 1, max: 20 }).map(String),
    horas_alto_consumo: fc.integer({ min: 0, max: 24 }).map(String),
    uso_horario_pico: fc.boolean(),
    antiguedad_inmueble: fc.oneof(
      fc.constant(''),
      fc.integer({ min: 2, max: 31 }).map(String)
    ),
    tiene_aire_acondicionado: fc.boolean(),
    tiene_calentador_electrico: fc.boolean(),
    electrodomesticos_eficientes: fc.boolean(),
  });
}

const EXPECTED_KEYS = [
  'consumo_kwh',
  'tipo_inmueble',
  'personas_vivienda',
  'cantidad_equipos',
  'horas_alto_consumo',
  'uso_horario_pico',
  'antiguedad_inmueble',
  'tiene_aire_acondicionado',
  'tiene_calentador_electrico',
  'electrodomesticos_eficientes',
];

const NUMERIC_FIELDS = [
  'consumo_kwh',
  'personas_vivienda',
  'cantidad_equipos',
  'horas_alto_consumo',
  'antiguedad_inmueble',
];

const TOGGLE_FIELDS = [
  'uso_horario_pico',
  'tiene_aire_acondicionado',
  'tiene_calentador_electrico',
  'electrodomesticos_eficientes',
];

const VALID_TIPO_INMUEBLE = ['Casa', 'Oficina', 'Apartamento', 'Comercio'];

describe('buildPayload - Property-Based Tests', () => {
  /**
   * Property 4: buildPayload produces structurally complete and type-correct payloads
   *
   * For any valid form data object, buildPayload SHALL produce an object with exactly
   * the 10 keys defined in the API contract where numeric fields are Number types,
   * toggle fields are 0 or 1 (not boolean), and tipo_inmueble is a capitalized string
   * from the valid set.
   *
   * **Validates: Requirements 3.5, 10.2, 10.3**
   */
  it('Property 4: buildPayload produces structurally complete and type-correct payloads', () => {
    fc.assert(
      fc.property(arbValidFormData(), (formData) => {
        const payload = buildPayload(formData);

        // Assert output has exactly 10 keys
        expect(Object.keys(payload).length).toBe(10);

        // Assert all expected keys are present
        for (const key of EXPECTED_KEYS) {
          expect(payload).toHaveProperty(key);
        }

        // Assert numeric fields are typeof "number"
        for (const field of NUMERIC_FIELDS) {
          expect(typeof payload[field]).toBe('number');
          expect(Number.isNaN(payload[field])).toBe(false);
        }

        // Assert toggle fields are 0 or 1 (not boolean)
        for (const field of TOGGLE_FIELDS) {
          expect(typeof payload[field]).not.toBe('boolean');
          expect(payload[field] === 0 || payload[field] === 1).toBe(true);
        }

        // Assert tipo_inmueble is capitalized and in valid set
        expect(VALID_TIPO_INMUEBLE).toContain(payload.tipo_inmueble);
      }),
      { numRuns: 30 }
    );
  });
});
