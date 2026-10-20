import React from "react";

const SCALE_MAX_KG = 500;
const numberFormat = new Intl.NumberFormat("es-CO", {
  maximumFractionDigits: 2,
});

function getLevel(value) {
  if (value <= 100) return { label: "Bajo", className: "low" };
  if (value <= 250) return { label: "Moderado", className: "moderate" };
  return { label: "Alto", className: "high" };
}

export default function CarbonFootprintIndicator({ result }) {
  if (!Object.prototype.hasOwnProperty.call(result, "huella_carbono_kg")) {
    return null;
  }

  const emissions = result.huella_carbono_kg;
  if (typeof emissions !== "number" || !Number.isFinite(emissions)) {
    return (
      <section className="carbon-footprint" aria-label="Huella de carbono">
        <div className="carbon-footprint-heading">
          <span className="eyebrow">Huella de carbono estimada</span>
          <span className="carbon-footprint-unavailable">No disponible</span>
        </div>
        <p className="carbon-footprint-caption">
          No hay un factor de emisión disponible para {result.pais || "este país"}.
        </p>
      </section>
    );
  }

  const level = getLevel(emissions);
  const progress = Math.min(Math.max((emissions / SCALE_MAX_KG) * 100, 0), 100);
  const progressWidth = Math.round(progress * 100) / 100;
  const accessibleValue = Math.min(Math.max(emissions, 0), SCALE_MAX_KG);

  return (
    <React.Fragment>
      <section className="carbon-footprint" aria-label="Huella de carbono">
        <div className="carbon-footprint-heading">
          <span className="eyebrow">Huella de carbono estimada</span>
          <span className={`carbon-footprint-level carbon-footprint-level--${level.className}`}>
            {level.label}
          </span>
        </div>
        <div className="carbon-footprint-value">
          {numberFormat.format(emissions)} <span>kg CO₂/mes</span>
        </div>
        <div
          className="carbon-footprint-track"
          role="progressbar"
          aria-label="Escala visual de emisiones mensuales"
          aria-valuemin={0}
          aria-valuemax={SCALE_MAX_KG}
          aria-valuenow={accessibleValue}
          aria-valuetext={`${numberFormat.format(emissions)} kg CO₂ al mes, nivel ${level.label.toLowerCase()}`}
        >
          <div
            className={`carbon-footprint-fill carbon-footprint-fill--${level.className}`}
            style={{ width: `${progressWidth}%` }}
          />
        </div>
        <div className="carbon-footprint-scale" aria-hidden="true">
          <span>0</span>
          <span>100</span>
          <span>250</span>
          <span>500+</span>
        </div>
        <p className="carbon-footprint-caption">
          Factor: {numberFormat.format(result.factor_emision_kg_co2_kwh)} kg CO₂/kWh ·{" "}
          {result.fuente_factor_emision}, {result.anio_factor_emision} (CC BY 4.0)
        </p>
        <p className="carbon-footprint-disclaimer">
          Niveles visuales orientativos; no son umbrales regulatorios.
        </p>
      </section>
    </React.Fragment>
  );
}
