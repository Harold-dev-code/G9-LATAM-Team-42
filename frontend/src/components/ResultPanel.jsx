import MeterGauge from "./MeterGauge";
import StatusBadge from "./StatusBadge";

const currency = new Intl.NumberFormat("es-DO", {
  style: "currency",
  currency: "USD",
  maximumFractionDigits: 2,
});

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
        <MeterGauge score={0} categoria={null} />
        <h3>Tu diagnóstico aparecerá aquí</h3>
        <p className="text-muted">
          Completa los datos de consumo y presiona “Calcular eficiencia” para ver la
          categoría, el costo estimado y las recomendaciones.
        </p>
      </div>
    );
  }

  const { categoria, probabilidad, recomendaciones, costo_estimado } = result;
  const confianzaModelo =
    typeof probabilidad === "number" ? `${(probabilidad * 100).toFixed(1)}%` : "No disponible";

  // Convert (categoria, probabilidad) into a 0-100 gauge score.
  // Gauge: 0=green/efficient (left), 100=red/inefficient (right).
  // probabilidad is confidence in the assigned category (0.0-1.0).
  function computeGaugeScore(cat, prob) {
    const p = prob ?? 0.5;
    switch (cat) {
      case "Eficiente":
        // High confidence → closer to 0 (deep green)
        return 33 - (p * 33);
      case "Moderado":
        // High confidence → closer to center (50)
        return 33 + (p * 33);
      case "Ineficiente":
        // High confidence → closer to 100 (deep red)
        return 66 + (p * 34);
      default:
        return 50;
    }
  }

  const gaugeScore = computeGaugeScore(categoria, probabilidad);

  return (
    <div className="result-panel">
      <div className="result-panel-header">
        <MeterGauge score={gaugeScore} categoria={categoria} />
        <div className="result-stat result-stat--inline">
          <span className="result-stat-label">Costo estimado mensual</span>
          <span className="result-stat-value mono">{currency.format(costo_estimado ?? 0)}</span>
        </div>
        <div className="result-stat result-stat--inline">
          <span className="result-stat-label">Confianza del modelo</span>
          <span className="result-stat-value mono">{confianzaModelo}</span>
        </div>
      </div>

      <div className="result-panel-heading result-panel-heading--corner">
        <span className="eyebrow">Resultado del análisis</span>
        <StatusBadge categoria={categoria} size="lg" />
      </div>

      {recomendaciones && recomendaciones.length > 0 && (
        <div className="recommendations">
          <span className="eyebrow">Recomendaciones</span>
          <ul>
            {recomendaciones.filter(Boolean).map((rec, idx) => {
              // Detectar si la recomendación contiene conversiones de moneda (COP, MXN, DOP, ARS)
              const hasConversion = /CONVERSION:/i.test(rec);
              if (hasConversion) {
                const [recPart, convPart] = rec.split(/CONVERSION:/i);
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
