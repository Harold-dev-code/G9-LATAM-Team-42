// Cliente para la API de EnergiAI (backend Spring Boot).
// Contrato de datos: docs/contrato-api.md en la raíz del monorepo.

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

class ApiError extends Error {
  constructor(message, status, details) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

async function parseErrorBody(response) {
  try {
    const body = await response.json();
    // GlobalExceptionHandler devuelve { error, messages: [...], path, ... }.
    if (Array.isArray(body?.messages) && body.messages.length > 0) {
      return body.messages.join(" · ");
    }
    if (body?.errors && typeof body.errors === "object") {
      return Object.values(body.errors).join(" · ");
    }
    return body?.message || body?.error || null;
  } catch {
    return null;
  }
}

/**
 * Envía los datos de consumo y recibe la clasificación de eficiencia.
 * @param {{
 *   consumo_kwh: number,
 *   tipo_inmueble: string,
 *   personas_vivienda: number,
 *   cantidad_equipos: number,
 *   horas_alto_consumo: number,
 *   uso_horario_pico: (0|1),
 *   antiguedad_inmueble: number,
 *   tiene_aire_acondicionado: (0|1),
 *   tiene_calentador_electrico: (0|1),
 *   electrodomesticos_eficientes: (0|1)
 * }} payload
 */
export async function postAnalisis(payload, userId) {
  const headers = { "Content-Type": "application/json" };
  if (userId) headers["X-User-Id"] = String(userId);

  const response = await fetch(`${BASE_URL}/analisis-energetico`, {
    method: "POST",
    headers,
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const detail = await parseErrorBody(response);
    throw new ApiError(
      detail || "No se pudo procesar el análisis. Verifica los datos ingresados.",
      response.status
    );
  }

  return response.json();
}

/** Obtiene el historial de análisis del usuario (o todos si no se envía userId). */
export async function getHistorial(userId) {
  const headers = {};
  if (userId) headers["X-User-Id"] = String(userId);

  const response = await fetch(`${BASE_URL}/analisis-energetico/historial`, { headers });

  if (!response.ok) {
    const detail = await parseErrorBody(response);
    throw new ApiError(detail || "No se pudo cargar el historial.", response.status);
  }

  return response.json();
}

/** Elimina un análisis del historial (por ejemplo, si se ingresaron datos por error). */
export async function deleteAnalisis(id) {
  const response = await fetch(`${BASE_URL}/analisis-energetico/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    const detail = await parseErrorBody(response);
    throw new ApiError(detail || "No se pudo eliminar el análisis.", response.status);
  }
}

export { ApiError, BASE_URL };
