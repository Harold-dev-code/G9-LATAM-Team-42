# EnergiAI ⚡🧠

EnergiAI es un **asesor energético automático** diseñado para hogares y pequeños negocios. A través de inteligencia artificial, analiza el perfil de consumo eléctrico de los usuarios para clasificar su eficiencia, estimar su gasto mensual y ofrecer recomendaciones personalizadas y accionables de ahorro.

El proyecto es desarrollado como parte de la simulación laboral de **No Country** por el equipo **G9-LATAM-Team-42**.

---

## 📋 ¿Qué hace EnergiAI? (Alcance del MVP)
Cuando un usuario envía sus datos de consumo, el sistema procesa la información y devuelve en segundos:
1. **Clasificación de Perfil:** Determina si el consumo es *Eficiente*, *Moderado* o *Ineficiente* usando un modelo de Machine Learning (Árbol de Decisión).
2. **Recomendaciones de Ahorro:** Entrega consejos prácticos y personalizados generados por IA (Gemini), adaptados al nivel de eficiencia.
3. **Costo Estimado:** Calcula una proyección del valor de la factura mensual basado en la tarifa configurable por el usuario (default $0.75 USD por kWh).
4. **Conversión de Moneda:** Gemini sugiere equivalentes del costo en monedas LATAM (COP, MXN, DOP, ARS).
5. **Historial:** El usuario puede consultar y eliminar análisis anteriores, con gráficos de evolución del consumo.

---

## 🛠️ Arquitectura y Flujo Técnico
El sistema está diseñado bajo una **arquitectura de microservicios**:

```
Frontend (React 19 + Vite 8, puerto 5173)
    ↓ POST /analisis-energetico
Backend (Java 21 + Spring Boot 4.1, puerto 8080)
    ↓ POST /predict
Servicio Flask (Python 3.12, scikit-learn, puerto 5000)
    → Modelo Árbol de Decisión (pipeline .pkl)
Backend ← Flask (categoria + probabilidad)
    ↓ Gemini API (recomendaciones + conversión moneda)
Backend → Frontend (categoria + probabilidad + recomendaciones + costo)
```

### Componentes:
1. **Frontend (React 19 + Vite 8):** SPA con formulario de 11 campos, gauge animado, panel de resultados, historial con gráficos (Recharts), tema dark/light, sidebar responsive.
2. **Backend (Java 21 + Spring Boot 4.1):** API Gateway que valida entrada, delega predicción a Flask, consulta Gemini para recomendaciones, calcula costos y persiste en BD.
3. **Data Science (Python 3.12 + Flask):** Microservicio que carga el modelo ML (.pkl), calcula features derivados y ejecuta predicción.
4. **IA Generativa (Gemini 3.5 Flash):** Genera recomendaciones personalizadas y sugerencias de conversión de moneda.
5. **Base de Datos:** H2 en memoria (dev) / Oracle Autonomous DB (prod).

---

## 🚀 Cómo ejecutar localmente

### Prerrequisitos
- Java 21, Maven
- Python 3.12, pip
- Node.js 20+

### 1. Servicio Flask (ML)
```bash
cd datascience/inference_service
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python app.py
# → Corre en http://localhost:5000
```

### 2. Backend (Spring Boot)
```bash
cd backend
chmod +x mvnw
./mvnw spring-boot:run
# → Corre en http://localhost:8080
```

### 3. Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
# → Corre en http://localhost:5173
```

### 4. Abrir en navegador
Ir a `http://localhost:5173` y usar el formulario "Nuevo análisis".

---

## 🧪 Tests

| Capa | Framework | Comando |
|------|-----------|---------|
| Frontend (unit + PBT) | Vitest + fast-check | `cd frontend && npm test` |
| Backend (unit + PBT) | JUnit 5 + jqwik | `cd backend && ./mvnw test` |
| Flask (unit + PBT) | pytest + Hypothesis | `cd datascience/inference_service && pytest` |

---

## 📁 Estructura del monorepo

```
G9-LATAM-Team-42/
├── frontend/          → React 19 + Vite 8 (SPA)
├── backend/           → Spring Boot 4.1 (API)
├── datascience/       → Modelo ML + Servicio Flask
│   ├── inference_service/  → Flask API de predicción
│   └── modelo_arbol_decision_pipeline.pkl
├── docs/              → Contrato API y documentación técnica
└── README.md
```

