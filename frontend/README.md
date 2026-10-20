# Frontend JouleAI

Interfaz de JouleAI desarrollada con **React 19**, **Vite 8** y CSS propio. Consume la API Spring Boot para registrar usuarios, analizar consumo y consultar el historial.

## Requisitos y arranque

Usa una versión de Node.js compatible con Vite 8 (20.19+ o 22.12+).

```bash
cd frontend
npm install
cp .env.example .env.local
npm run dev
```

Vite sirve el frontend en **http://localhost:5173**. Configura `VITE_API_URL` en `.env.local` si el backend no está en `http://localhost:8080`. El backend permite CORS para el origen local `http://localhost:5173`.

## Funcionalidades principales

- Registro e inicio de sesión.
- Formulario de análisis con los datos del consumo, inmueble y período facturado.
- Resultado con categoría, costo estimado, confianza del modelo, recomendaciones y barra lineal de huella de carbono cuando la API devuelve un factor conocido.
- Historial con gráficos y tabla, además de generación de reportes PDF.
- Selector de **15 países de LATAM**: Colombia (COP), México (MXN), Chile (CLP), Argentina (ARS), Perú (PEN), Brasil (BRL), Uruguay (UYU), Ecuador (USD), Guatemala (GTQ), Honduras (HNL), Paraguay (PYG), Bolivia (BOB), Costa Rica (CRC), República Dominicana (DOP) y Venezuela (VES).

La huella se presenta en kg CO₂/mes. La barra usa una escala visual hasta 500 kg: bajo (hasta 100), moderado (más de 100 y hasta 250) y alto (más de 250). Son niveles orientativos definidos por la interfaz, no umbrales regulatorios. También se muestra el factor usado, su fuente Ember y el año 2024. Si no hay un factor en la respuesta, se informa que la estimación no está disponible.

## Contrato de datos

El formulario envía al backend un JSON `snake_case` con los campos del análisis, incluidos `consumo_kwh`, `tipo_inmueble`, `pais`, `moneda`, `periodo`, `etiqueta`, `personas_vivienda`, `cantidad_equipos`, `horas_alto_consumo`, `uso_horario_pico`, `antiguedad_inmueble`, indicadores de equipos, `tarifa_kwh`, nombre/dirección del inmueble y mes/año facturados.

El resultado incluye categoría, probabilidad, recomendaciones, estimaciones de costo y, cuando hay datos disponibles, `huella_carbono_kg`, `factor_emision_kg_co2_kwh`, `anio_factor_emision` y `fuente_factor_emision`. Los objetos del historial utilizan campos camelCase diferentes, por ejemplo `consumoKwh`, `fechaCreacion` y `nombreInmueble`.

Consulta [`../docs/contrato-api.md`](../docs/contrato-api.md) para detalles, rangos de validación y ejemplos completos.

## Estructura

```text
src/
  api/energiaiClient.js
  components/
    AnalysisForm.jsx
    ResultPanel.jsx
    CarbonFootprintIndicator.jsx
    MeterGauge.jsx
    HistoryView.jsx
    ReportsView.jsx
  utils/
  hooks/
```

`CarbonFootprintIndicator` muestra la estimación recibida del backend; no recalcula factores ni persiste información. El historial refleja los campos que entrega el endpoint actual y todavía no guarda ni muestra la huella de carbono.

## Pruebas y build

```bash
npm test
npm run lint
npm run build
```

En Docker Compose, el frontend compilado se publica por el puerto `80`. El `5173` es el puerto del servidor de desarrollo de Vite.
