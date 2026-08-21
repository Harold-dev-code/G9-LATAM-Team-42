import { useState } from "react";
import { validateAnalysisForm } from "../utils/validateAnalysisForm";
import { buildPayload } from "../utils/buildPayload";

const INMUEBLES = [
  { value: "casa", label: "Casa" },
  { value: "oficina", label: "Oficina" },
  { value: "apartamento", label: "Apartamento" },
  { value: "comercio", label: "Comercio" },
];

const INITIAL_FORM = {
  consumo_kwh: "",
  tipo_inmueble: "casa",
  personas_vivienda: "",
  cantidad_equipos: "",
  horas_alto_consumo: "",
  uso_horario_pico: false,
  antiguedad_inmueble: "",
  tiene_aire_acondicionado: false,
  tiene_calentador_electrico: false,
  electrodomesticos_eficientes: false,
  tarifa_kwh: "0.75",
};

export default function AnalysisForm({ onSubmit, submitting }) {
  const [form, setForm] = useState(INITIAL_FORM);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function handleBlur(field) {
    setTouched((prev) => ({ ...prev, [field]: true }));
    setErrors(validateAnalysisForm(form));
  }

  function handleSubmit(event) {
    event.preventDefault();
    const validation = validateAnalysisForm(form);
    setErrors(validation);
    setTouched({
      consumo_kwh: true,
      tipo_inmueble: true,
      personas_vivienda: true,
      cantidad_equipos: true,
      horas_alto_consumo: true,
      uso_horario_pico: true,
      antiguedad_inmueble: true,
      tiene_aire_acondicionado: true,
      tiene_calentador_electrico: true,
      electrodomesticos_eficientes: true,
    });
    if (Object.keys(validation).length > 0) return;

    onSubmit(buildPayload(form));
  }

  return (
    <form className="analysis-form" onSubmit={handleSubmit} noValidate>
      <div className="form-field">
        <label htmlFor="consumo_kwh">Consumo mensual</label>
        <div className="input-with-suffix">
          <input
            id="consumo_kwh"
            type="number"
            min="0"
            step="0.1"
            inputMode="decimal"
            placeholder="450.5"
            value={form.consumo_kwh}
            onChange={(e) => update("consumo_kwh", e.target.value)}
            onBlur={() => handleBlur("consumo_kwh")}
            aria-invalid={touched.consumo_kwh && !!errors.consumo_kwh}
            aria-describedby="consumo_kwh-hint"
          />
          <span className="input-suffix mono">kWh</span>
        </div>
        {touched.consumo_kwh && errors.consumo_kwh ? (
          <p className="field-error" id="consumo_kwh-hint">{errors.consumo_kwh}</p>
        ) : (
          <p className="field-hint" id="consumo_kwh-hint">Lo que indica tu última factura de luz.</p>
        )}
      </div>

      <div className="form-row">
        <div className="form-field">
          <label htmlFor="personas_vivienda">Personas dentro del Inmueble</label>
          <input
            id="personas_vivienda"
            type="number"
            min="1"
            max="10"
            step="1"
            inputMode="numeric"
            placeholder="4"
            value={form.personas_vivienda}
            onChange={(e) => update("personas_vivienda", e.target.value)}
            onBlur={() => handleBlur("personas_vivienda")}
            aria-invalid={touched.personas_vivienda && !!errors.personas_vivienda}
            aria-describedby="personas_vivienda-error"
          />
          {touched.personas_vivienda && errors.personas_vivienda && (
            <p className="field-error" id="personas_vivienda-error">{errors.personas_vivienda}</p>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="cantidad_equipos">Equipos conectados</label>
          <input
            id="cantidad_equipos"
            type="number"
            min="1"
            max="20"
            step="1"
            inputMode="numeric"
            placeholder="12"
            value={form.cantidad_equipos}
            onChange={(e) => update("cantidad_equipos", e.target.value)}
            onBlur={() => handleBlur("cantidad_equipos")}
            aria-invalid={touched.cantidad_equipos && !!errors.cantidad_equipos}
            aria-describedby="cantidad_equipos-error"
          />
          {touched.cantidad_equipos && errors.cantidad_equipos && (
            <p className="field-error" id="cantidad_equipos-error">{errors.cantidad_equipos}</p>
          )}
        </div>
      </div>

      <div className="form-row">
        <div className="form-field">
          <label htmlFor="horas_alto_consumo">Horas de alto consumo / día</label>
          <input
            id="horas_alto_consumo"
            type="number"
            min="0"
            max="24"
            step="1"
            inputMode="numeric"
            placeholder="6"
            value={form.horas_alto_consumo}
            onChange={(e) => update("horas_alto_consumo", e.target.value)}
            onBlur={() => handleBlur("horas_alto_consumo")}
            aria-invalid={touched.horas_alto_consumo && !!errors.horas_alto_consumo}
            aria-describedby="horas_alto_consumo-error"
          />
          {touched.horas_alto_consumo && errors.horas_alto_consumo && (
            <p className="field-error" id="horas_alto_consumo-error">{errors.horas_alto_consumo}</p>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="antiguedad_inmueble">Antigüedad del inmueble (años)</label>
          <input
            id="antiguedad_inmueble"
            type="number"
            min="2"
            max="31"
            step="1"
            inputMode="numeric"
            placeholder="10"
            value={form.antiguedad_inmueble}
            onChange={(e) => update("antiguedad_inmueble", e.target.value)}
            onBlur={() => handleBlur("antiguedad_inmueble")}
            aria-invalid={touched.antiguedad_inmueble && !!errors.antiguedad_inmueble}
            aria-describedby="antiguedad_inmueble-error"
          />
          {touched.antiguedad_inmueble && errors.antiguedad_inmueble && (
            <p className="field-error" id="antiguedad_inmueble-error">{errors.antiguedad_inmueble}</p>
          )}
        </div>
      </div>

      <div className="form-field">
        <label htmlFor="tipo_inmueble">Tipo de inmueble</label>
        <div className="segmented" role="radiogroup" aria-label="Tipo de inmueble">
          {INMUEBLES.map((opt) => (
            <button
              type="button"
              key={opt.value}
              role="radio"
              aria-checked={form.tipo_inmueble === opt.value}
              className={`segmented-option ${form.tipo_inmueble === opt.value ? "is-active" : ""}`}
              onClick={() => update("tipo_inmueble", opt.value)}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      <div className="form-field">
        <label htmlFor="tarifa_kwh">Tarifa por kWh (USD)</label>
        <div className="input-with-suffix">
          <input
            id="tarifa_kwh"
            type="number"
            min="0.01"
            max="5"
            step="0.01"
            inputMode="decimal"
            placeholder="0.75"
            value={form.tarifa_kwh}
            onChange={(e) => update("tarifa_kwh", e.target.value)}
            onBlur={() => handleBlur("tarifa_kwh")}
            aria-invalid={touched.tarifa_kwh && !!errors.tarifa_kwh}
            aria-describedby="tarifa_kwh-hint"
          />
          <span className="input-suffix mono">USD</span>
        </div>
        {touched.tarifa_kwh && errors.tarifa_kwh ? (
          <p className="field-error" id="tarifa_kwh-hint">{errors.tarifa_kwh}</p>
        ) : (
          <p className="field-hint" id="tarifa_kwh-hint">Precio del kilovatio-hora en tu región.</p>
        )}
      </div>

      <label className="toggle-row" htmlFor="uso_horario_pico">
        <span>
          <span className="toggle-title">Mayor uso en horario pico</span>
          <span className="toggle-subtitle">18:00 – 23:00</span>
        </span>
        <span className={`toggle ${form.uso_horario_pico ? "is-on" : ""}`}>
          <input
            id="uso_horario_pico"
            type="checkbox"
            checked={form.uso_horario_pico}
            onChange={(e) => update("uso_horario_pico", e.target.checked)}
          />
          <span className="toggle-knob" />
        </span>
      </label>

      <label className="toggle-row" htmlFor="tiene_aire_acondicionado">
        <span>
          <span className="toggle-title">Tiene aire acondicionado</span>
        </span>
        <span className={`toggle ${form.tiene_aire_acondicionado ? "is-on" : ""}`}>
          <input
            id="tiene_aire_acondicionado"
            type="checkbox"
            checked={form.tiene_aire_acondicionado}
            onChange={(e) => update("tiene_aire_acondicionado", e.target.checked)}
          />
          <span className="toggle-knob" />
        </span>
      </label>

      <label className="toggle-row" htmlFor="tiene_calentador_electrico">
        <span>
          <span className="toggle-title">Tiene calentador eléctrico</span>
        </span>
        <span className={`toggle ${form.tiene_calentador_electrico ? "is-on" : ""}`}>
          <input
            id="tiene_calentador_electrico"
            type="checkbox"
            checked={form.tiene_calentador_electrico}
            onChange={(e) => update("tiene_calentador_electrico", e.target.checked)}
          />
          <span className="toggle-knob" />
        </span>
      </label>

      <label className="toggle-row" htmlFor="electrodomesticos_eficientes">
        <span>
          <span className="toggle-title">Electrodomésticos eficientes</span>
        </span>
        <span className={`toggle ${form.electrodomesticos_eficientes ? "is-on" : ""}`}>
          <input
            id="electrodomesticos_eficientes"
            type="checkbox"
            checked={form.electrodomesticos_eficientes}
            onChange={(e) => update("electrodomesticos_eficientes", e.target.checked)}
          />
          <span className="toggle-knob" />
        </span>
      </label>

      <button type="submit" className="btn-primary" disabled={submitting}>
        {submitting ? "Calculando…" : "Calcular eficiencia"}
      </button>
    </form>
  );
}
