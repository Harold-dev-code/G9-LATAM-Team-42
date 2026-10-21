import MeterGauge from "./MeterGauge";
import StatusBadge from "./StatusBadge";
import CarbonFootprintIndicator from "./CarbonFootprintIndicator";

const currency = new Intl.NumberFormat("es-DO", {
  style: "currency",
  currency: "USD",
  maximumFractionDigits: 2,
});

function formatMoney(value, currencyCode) {
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: currencyCode || "USD",
    maximumFractionDigits: 2,
  }).format(value ?? 0);
}

export default function ResultPanel({ result, error, loading }) {
  if (loading) {
    return (
      <div className="result-panel result-panel--empty">
        <div className="meter-gauge meter-gauge--skeleton" />
        <p className="text-muted">Consultando el modelo de JouleAI…</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="result-panel result-panel--error">
        <h3>No se pudo completar el análisis</h3>
        <p className="text-muted">{error}</p>
      </div>
    );
  }

  if (!result) {
    return (
      <div className="result-panel result-panel--empty">
        <div className="result-panel-empty-gauge">
          <MeterGauge categoria={null} size={180} />
        </div>
        <div className="result-panel-empty-message">
          <br />
          <h3>Tu diagnóstico aparecerá aquí</h3>
          <b></b>
          <p className="text-muted">
            Completa los datos de consumo y presiona “Calcular eficiencia” para ver la
            categoría, el costo estimado y las recomendaciones.
          </p>
        </div>
      </div>
    );
  }

  const {
    categoria,
    probabilidad,
    recomendaciones,
    costo_estimado,
    costo_estimado_usd,
    costo_estimado_local,
    moneda,
  } = result;
  const costoUsd = costo_estimado_usd ?? costo_estimado ?? 0;
  const confianzaModelo =
    typeof probabilidad === "number" ? `${(probabilidad * 100).toFixed(1)}%` : "No disponible";

  return (
    <div className="result-panel">
      <div className="result-panel-title">
        <span className="eyebrow">Resultado del análisis</span>
        <StatusBadge categoria={categoria} size="lg" />
      </div>

      <div className="result-panel-header">
        <MeterGauge categoria={categoria} size={190} />
      </div>

      <div className="result-panel-metrics">
        <div className="result-stat result-stat--inline">
          <span className="result-stat-label">Costo mensual en {moneda || "USD"}</span>
          <span className="result-stat-value mono">{formatMoney(costo_estimado_local ?? costoUsd, moneda || "USD")}</span>
          <span className="result-stat-secondary mono">Equivalente: {currency.format(costoUsd)}</span>
        </div>
        <div className="result-stat result-stat--inline">
          <span className="result-stat-label">Confianza del modelo</span>
          <span className="result-stat-value mono">{confianzaModelo}</span>
        </div>
      </div>

      <CarbonFootprintIndicator result={result} />

      {recomendaciones && recomendaciones.length > 0 && (
        <div className="recommendations result-panel-recommendations">
          <span className="eyebrow">Recomendaciones</span>
          <ul>
            {recomendaciones
              .filter(Boolean)
              .filter((rec) => !/CONVERSI[ÓO]N(?:\s+ESTIMADA)?:/i.test(rec))
              .map((rec, idx) => {
              // Detectar si la recomendación contiene conversiones de moneda (COP, MXN, DOP, ARS)
              const conversionRegex = /CONVERSI[ÓO]N(?:\s+ESTIMADA)?:/i;
              const hasConversion = conversionRegex.test(rec) || /CONVERSION:/i.test(rec);
              if (hasConversion) {
                const [recPart, convPart] = rec.split(conversionRegex);
                const recText = recPart?.replace(/RECOMENDACION:/i, "").replace(/\*\*/g, "").trim();
                const convLines = (convPart || "").split(/\n/).filter(l => l.trim().length > 0);
                return (
                  <li key={idx} className="rec-with-currency">
                    {recText && <p>{recText}</p>}
                    {convLines.length > 0 && (
                      <table className="currency-table">
                        <thead>
                          <tr><th>País / Moneda</th><th>Equivalente aproximado</th></tr>
                        </thead>
                        <tbody>
                          {convLines.map((line, i) => {
                            const cleaned = line.replace(/^\*\s*/, "").trim();
                            const colonIdx = cleaned.lastIndexOf(":");
                            if (colonIdx > 0) {
                              return (
                                <tr key={i}>
                                  <td>{cleaned.substring(0, colonIdx).trim()}</td>
                                  <td>{cleaned.substring(colonIdx + 1).trim()}</td>
                                </tr>
                              );
                            }
                            return <tr key={i}><td colSpan={2}>{cleaned}</td></tr>;
                          })}
                        </tbody>
                      </table>
                    )}
                  </li>
                );
              }
              const hasCurrency = /\b(COP|MXN|DOP|ARS|BRL|CLP|PEN|UYU|PYG|HNL|VES)\b/i.test(rec);
              if (hasCurrency) {
                const lines = rec.split(/\n/).filter(l => l.trim());
                const recLines = lines.filter(l => !/\b(COP|MXN|DOP|ARS|BRL|CLP|PEN|UYU|PYG|HNL|VES)\b/i.test(l));
                const currencyLines = lines.filter(l => /\b(COP|MXN|DOP|ARS|BRL|CLP|PEN|UYU|PYG|HNL|VES)\b/i.test(l));
                return (
                  <li key={idx} className="rec-with-currency">
                    {recLines.length > 0 && <p>{recLines.join(" ").replace(/\*\*/g, "").trim()}</p>}
                    {currencyLines.length > 0 && (
                      <table className="currency-table">
                        <thead>
                          <tr><th>País / Moneda</th><th>Equivalente aproximado</th></tr>
                        </thead>
                        <tbody>
                          {currencyLines.map((line, i) => {
                            const cleaned = line.replace(/^\*\s*/, "").trim();
                            const colonIdx = cleaned.lastIndexOf(":");
                            if (colonIdx > 0) {
                              return (
                                <tr key={i}>
                                  <td>{cleaned.substring(0, colonIdx).trim()}</td>
                                  <td>{cleaned.substring(colonIdx + 1).trim()}</td>
                                </tr>
                              );
                            }
                            return <tr key={i}><td colSpan={2}>{cleaned}</td></tr>;
                          })}
                        </tbody>
                      </table>
                    )}
                  </li>
                );
              }
              return <li key={idx}>{rec.replace(/\*\*/g, "")}</li>;
            })}
          </ul>
        </div>
      )}
    </div>
  );
}
