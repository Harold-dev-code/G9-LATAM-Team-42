# JouleAI ⚡ [![Demo en Vivo](https://img.shields.io/badge/DEMO-EN%20VIVO-brightgreen?style=for-the-badge&logo=googlechrome&logoColor=white)](http://129.80.145.212)
<!-- ===== BADGES (pegar justo debajo del título) ===== -->
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Python](https://img.shields.io/badge/Python-3.12-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![scikit-learn](https://img.shields.io/badge/scikit--learn-ML-F7931E?style=for-the-badge&logo=scikitlearn&logoColor=white)](https://scikit-learn.org/)
[![Gemini](https://img.shields.io/badge/Gemini-3.5%20Flash-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![OCI](https://img.shields.io/badge/Oracle%20Cloud-Infrastructure-F80000?style=for-the-badge&logo=oracle&logoColor=white)](https://www.oracle.com/cloud/)
[![License](https://img.shields.io/badge/status-hackathon-lightgrey?style=for-the-badge)]()
<!-- ==================================================== -->
JouleAI es un **asesor energético automático** diseñado para hogares y pequeños negocios. A través de inteligencia artificial, analiza el perfil de consumo eléctrico de los usuarios para clasificar su eficiencia, estimar su gasto mensual y ofrecer recomendaciones personalizadas y accionables de ahorro.
## Objetivos del Proyecto
* *Optimización del Consumo:* Proveer a los usuarios una herramienta digital accesible para auditar y comprender su gasto eléctrico mensual.
* *Automatización con Inteligencia Artificial:* Integrar un modelo de machine learning capaz de clasificar de manera automática el perfil de consumo (Eficiente, Moderado, Ineficiente).
* *Experiencia de Usuario Fluida:* Desarrollar una interfaz moderna, interactiva y responsiva con visualización de datos en tiempo real.
El proyecto es desarrollado como parte de la simulación laboral de **No Country** por el equipo **G9-LATAM-Team-42**.

---

## ¿Qué hace JouleAI?
Cuando un usuario se registra y envía sus datos de consumo, el sistema procesa la información y devuelve en segundos:
1. **Clasificación de Perfil:** Determina si el consumo es *Eficiente*, *Moderado* o *Ineficiente* usando un modelo de Machine Learning (Árbol de Decisión).
2. **Recomendaciones de Ahorro:** Entrega consejos prácticos y personalizados generados por IA (Gemini), adaptados al nivel de eficiencia.
3. **Costo Estimado:** Calcula una proyección del valor de la factura mensual basado en la tarifa configurable por el usuario (default $0.75 USD por kWh).
4. **Conversión de Moneda:** Gemini sugiere equivalentes del costo en 13 monedas de LATAM (ARS, BRL, CLP, COP, DOP, HNL, MXN, PEN, PYG, UYU, VES + USD).
5. **Historial Personal:** Cada usuario ve solo sus análisis, con gráficos de evolución del consumo.
6. **Reportes PDF:** Descarga reportes personalizados con nombre del usuario, gráficas de consumo, y resumen estadístico.

---

## Arquitectura y Flujo Técnico

```
Frontend (React 19 + Vite 8)
    ↓ POST /auth/register | /auth/login
    ↓ POST /analisis-energetico (+ header X-User-Id)
Backend (Java 21 + Spring Boot 4.1)
    ↓ POST /predict
Servicio Flask (Python 3.12 + scikit-learn)
    → Modelo Árbol de Decisión (15 features, pipeline .pkl)
Backend ← Flask (categoria + probabilidad)
    ↓ Gemini API (recomendación + conversión de moneda LATAM)
Backend → Frontend (categoria + probabilidad + recomendaciones + costo)
    → Oracle Cloud DB (persistencia)
```

### Componentes:
| Servicio | Tecnología | Puerto | Descripción |
|----------|-----------|--------|-------------|
| Frontend | React 19 + Vite 8 | 5173 (dev) / 80 (prod) | SPA con auth, formulario 11 campos, gauge, reportes PDF |
| Backend | Java 21 + Spring Boot 4.1 | 8080 | API REST, auth BCrypt, integración Flask + Gemini |
| Flask ML | Python 3.12 + scikit-learn | 5000 | Microservicio de inferencia con modelo .pkl |
| Gemini | Google AI (Gemini 3.5 Flash) | — | Recomendaciones + conversión de moneda |
| Base de datos | H2 (dev) / Oracle Autonomous (prod) | — | Persistencia de usuarios y análisis |

---
## Infraestructura y Despliegue (OCI)

JouleAI está desplegado en **Oracle Cloud Infrastructure (OCI)** usando una instancia de cómputo Ubuntu que orquesta los tres servicios (Frontend, Backend, Data Science) vía **Docker Compose**, con persistencia en una **Autonomous Database** protegida por ACL + TLS.

🔗 **Demo en vivo:** [http://129.80.145.212](http://129.80.145.212)

```text
[Cliente / Internet]
        │  IP pública: 129.80.145.212
┌───────▼─────────────────────────────────┐
│ OCI Compute Instance (Ubuntu + Docker)   │
│ ├─ frontend-app (Nginx)      → :80       │
│ ├─ backend-springboot (Java) → :8080     │
│ └─ python-service (ML)       → :5000     │
└───────▼─────────────────────────────────┘
        │  TLS cifrado, solo IP autorizada (ACL)
┌───────▼─────────────────────────────────┐
│ OCI Autonomous Database                  │
└───────────────────────────────────────────┘
```

| Componente | Detalle |
|---|---|
| Cómputo | Instancia Ubuntu, Docker Compose multi-contenedor, `restart: always` |
| Base de datos | Autonomous DB (tablas `USUARIO`, `ANALISIS_ENERGETICO`), acceso restringido por ACL a la IP del servidor |
| Red | VCN + Security List, puertos `80` (Frontend) y `8080` (Backend) expuestos |
| Secretos | Variables sensibles (`GEMINI_API_KEY`, credenciales DB) aisladas en `.env`, fuera del control de versiones |

📄 Documentación técnica completa (comandos de despliegue, mantenimiento, diagrama detallado): **[docs/OCI.md](./docs/OCI.md)**
## Cómo ejecutar localmente

### Prerrequisitos
- Java 21, Maven
- Python 3.12, pip
- Node.js 20+
- (Opcional) Docker + Docker Compose

### Opción A: Servicios individuales (3 terminales)

**Terminal 1 — Flask ML:**
```bash
cd datascience/inference_service
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

**Terminal 2 — Backend:**
```bash
cd backend
chmod +x mvnw
GEMINI_API_KEY=tu-key SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

**Terminal 3 — Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Abrir `http://localhost:5173`

### Opción B: Docker Compose (un comando)
```bash
cp .env.example .env
# Editar .env con tu GEMINI_API_KEY
docker compose up --build
```

Abrir `http://localhost:3000`

---

## Tests

| Capa | Framework | Comando | Tests |
|------|-----------|---------|-------|
| Frontend | Vitest + fast-check | `cd frontend && npm test` | 11 (unit + PBT) |
| Backend | JUnit 5 + jqwik | `cd backend && ./mvnw test` | 20 (unit + PBT) |
| Flask | pytest + Hypothesis | `cd datascience/inference_service && pytest` | 91 (unit + PBT) |

---

## Estructura del monorepo

```
G9-LATAM-Team-42/
├── frontend/                → React 19 + Vite 8 (SPA con auth)
├── backend/                 → Spring Boot 4.1 (API + auth + Gemini)
├── datascience/             → Modelo ML + Servicio Flask
│   ├── inference_service/   → Flask API de predicción
│   └── modelo_arbol_decision_pipeline.pkl
├── docs/                    → Contrato API + historial de desarrollo
├── docker-compose.yml       → Orquestación de servicios
├── .env.example             → Template de variables de entorno
└── README.md
```

---

## Equipo
**G9 LATAM Team 42** — Simulación laboral No Country (2026)
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

## Tests

| Capa | Framework | Comando |
|------|-----------|---------|
| Frontend (unit + PBT) | Vitest + fast-check | `cd frontend && npm test` |
| Backend (unit + PBT) | JUnit 5 + jqwik | `cd backend && ./mvnw test` |
| Flask (unit + PBT) | pytest + Hypothesis | `cd datascience/inference_service && pytest` |

---

## Estructura del monorepo

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

