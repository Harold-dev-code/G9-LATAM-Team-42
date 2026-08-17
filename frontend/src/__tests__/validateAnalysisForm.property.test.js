import { describe, it, expect } from 'vitest';
import fc from 'fast-check';
import { validateAnalysisForm } from '../utils/validateAnalysisForm.js';
import { isEqual } from 'lodash';

/**
 * Custom arbitraries for property-based testing of validateAnalysisForm.
 */

/** Generates form data where ALL required fields are within valid ranges. */
function arbValidFormData() {
  return fc.record({
    consumo_kwh: fc.double({ min: 50, max: 2000, noNaN: true }).map(String),
    tipo_inmueble: fc.constantFrom('casa', 'oficina', 'apartamento', 'comercio'),
    personas_vivienda: fc.integer({ min: 1, max: 10 }).map(String),
    cantidad_equipos: fc.integer({ min: 1, max: 20 }).map(String),
    horas_alto_consumo: fc.double({ min: 0, max: 24, noNaN: true }).map(String),
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

/** Generates form data where at least one required field is out of range. */
function arbInvalidFormData() {
  const invalidConsumo = fc.oneof(
    fc.constant(''),
    fc.double({ min: -1000, max: 49.99, noNaN: true }).map(String),
    fc.double({ min: 2000.01, max: 100000, noNaN: true }).map(String),
    fc.constant('abc')
  );
  const invalidPersonas = fc.oneof(
    fc.constant(''),
    fc.integer({ min: -100, max: 0 }).map(String),
    fc.integer({ min: 11, max: 1000 }).map(String),
    fc.constant('xyz')
  );
  const invalidEquipos = fc.oneof(
    fc.constant(''),
    fc.integer({ min: -100, max: 0 }).map(String),
    fc.integer({ min: 21, max: 1000 }).map(String),
    fc.constant('nope')
  );
  const invalidHoras = fc.oneof(
    fc.constant(''),
    fc.double({ min: -100, max: -0.01, noNaN: true }).map(String),
    fc.double({ min: 24.01, max: 1000, noNaN: true }).map(String),
    fc.constant('bad')
  );
  const invalidTipo = fc.oneof(
    fc.constant(''),
    fc.constant('bodega'),
    fc.constant('hotel'),
    fc.constant(null)
  );

  return fc.record({
    corruptConsumo: fc.boolean(),
    corruptPersonas: fc.boolean(),
    corruptEquipos: fc.boolean(),
    corruptHoras: fc.boolean(),
    corruptTipo: fc.boolean(),
    invalidConsumoVal: invalidConsumo,
    invalidPersonasVal: invalidPersonas,
    invalidEquiposVal: invalidEquipos,
    invalidHorasVal: invalidHoras,
    invalidTipoVal: invalidTipo,
    validConsumo: fc.double({ min: 50, max: 2000, noNaN: true }).map(String),
    validPersonas: fc.integer({ min: 1, max: 10 }).map(String),
    validEquipos: fc.integer({ min: 1, max: 20 }).map(String),
    validHoras: fc.double({ min: 0, max: 24, noNaN: true }).map(String),
    validTipo: fc.constantFrom('casa', 'oficina', 'apartamento', 'comercio'),
    uso_horario_pico: fc.boolean(),
    antiguedad_inmueble: fc.oneof(fc.constant(''), fc.integer({ min: 2, max: 31 }).map(String)),
    tiene_aire_acondicionado: fc.boolean(),
    tiene_calentador_electrico: fc.boolean(),
    electrodomesticos_eficientes: fc.boolean(),
  }).filter(d => d.corruptConsumo || d.corruptPersonas || d.corruptEquipos || d.corruptHoras || d.corruptTipo)
    .map(d => {
      const corruptedFields = [];
      const formData = {
        consumo_kwh: d.corruptConsumo ? d.invalidConsumoVal : d.validConsumo,
        tipo_inmueble: d.corruptTipo ? d.invalidTipoVal : d.validTipo,
        personas_vivienda: d.corruptPersonas ? d.invalidPersonasVal : d.validPersonas,
        cantidad_equipos: d.corruptEquipos ? d.invalidEquiposVal : d.validEquipos,
        horas_alto_consumo: d.corruptHoras ? d.invalidHorasVal : d.validHoras,
        uso_horario_pico: d.uso_horario_pico,
        antiguedad_inmueble: d.antiguedad_inmueble,
        tiene_aire_acondicionado: d.tiene_aire_acondicionado,
        tiene_calentador_electrico: d.tiene_calentador_electrico,
        electrodomesticos_eficientes: d.electrodomesticos_eficientes,
      };

      if (d.corruptConsumo) corruptedFields.push('consumo_kwh');
      if (d.corruptPersonas) corruptedFields.push('personas_vivienda');
      if (d.corruptEquipos) corruptedFields.push('cantidad_equipos');
      if (d.corruptHoras) corruptedFields.push('horas_alto_consumo');
      if (d.corruptTipo) corruptedFields.push('tipo_inmueble');

      return { formData, corruptedFields };
    });
}

/** Generates completely random form data (any types, any values). */
function arbArbitraryFormData() {
  return fc.record({
    consumo_kwh: fc.oneof(fc.string(), fc.double().map(String), fc.constant(null), fc.constant(undefined), fc.constant('')),
    tipo_inmueble: fc.oneof(fc.string(), fc.constant(null), fc.constant(undefined), fc.constantFrom('casa', 'oficina', 'apartamento', 'comercio')),
    personas_vivienda: fc.oneof(fc.string(), fc.integer().map(String), fc.constant(null), fc.constant(undefined), fc.constant('')),
    cantidad_equipos: fc.oneof(fc.string(), fc.integer().map(String), fc.constant(null), fc.constant(undefined), fc.constant('')),
    horas_alto_consumo: fc.oneof(fc.string(), fc.double().map(String), fc.constant(null), fc.constant(undefined), fc.constant('')),
    uso_horario_pico: fc.oneof(fc.boolean(), fc.constant(null), fc.constant(undefined), fc.string()),
    antiguedad_inmueble: fc.oneof(fc.string(), fc.integer().map(String), fc.constant(null), fc.constant(undefined), fc.constant('')),
    tiene_aire_acondicionado: fc.oneof(fc.boolean(), fc.constant(null), fc.constant(undefined)),
    tiene_calentador_electrico: fc.oneof(fc.boolean(), fc.constant(null), fc.constant(undefined)),
    electrodomesticos_eficientes: fc.oneof(fc.boolean(), fc.constant(null), fc.constant(undefined)),
  });
}

describe('validateAnalysisForm - Property-Based Tests', () => {
  /**
   * Property 1: Valid form data produces no validation errors
   * **Validates: Requirements 9.2**
   */
  it('Property 1: valid form data produces no validation errors', () => {
    fc.assert(
      fc.property(arbValidFormData(), (validData) => {
        const errors = validateAnalysisForm(validData);
        expect(Object.keys(errors).length).toBe(0);
      }),
      { numRuns: 30 }
    );
  });

  /**
   * Property 2: Out-of-range fields produce corresponding validation errors
   * **Validates: Requirements 3.4, 9.3**
   */
  it('Property 2: out-of-range fields produce corresponding validation errors', () => {
    fc.assert(
      fc.property(arbInvalidFormData(), ({ formData, corruptedFields }) => {
        const errors = validateAnalysisForm(formData);
        // Result must be non-empty
        expect(Object.keys(errors).length).toBeGreaterThan(0);
        // Each corrupted field must have a corresponding error key
        for (const field of corruptedFields) {
          expect(errors).toHaveProperty(field);
        }
      }),
      { numRuns: 30 }
    );
  });

  /**
   * Property 3: Validation is idempotent
   * **Validates: Requirements 9.4**
   */
  it('Property 3: validation is idempotent', () => {
    fc.assert(
      fc.property(arbArbitraryFormData(), (data) => {
        const result1 = validateAnalysisForm(data);
        const result2 = validateAnalysisForm(data);
        expect(isEqual(result1, result2)).toBe(true);
      }),
      { numRuns: 30 }
    );
  });
});
