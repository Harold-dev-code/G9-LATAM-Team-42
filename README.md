# JouleAI ⚡ 
<!-- ===== BADGES (pegar justo debajo del título) ===== -->
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Python](https://img.shields.io/badge/Python-3.12-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![Jupyter Notebook](https://img.shields.io/badge/Jupyter-F37626?style=for-the-badge&logo=jupyter&logoColor=white)](https://jupyter.org/)
[![Gemini](https://img.shields.io/badge/Gemini-3.5%20Flash-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![OCI](https://img.shields.io/badge/Oracle%20Cloud-Infrastructure-F80000?style=for-the-badge&logo=oracle&logoColor=white)](https://www.oracle.com/cloud/)
[![License](https://img.shields.io/badge/status-hackathon-lightgrey?style=for-the-badge)]()
<!-- ==================================================== -->

**JouleAI analiza el consumo eléctrico mensual de hogares y pequeños negocios. Clasifica el perfil energético, estima el costo en dolares y moneda local, calcula una huella de carbono orientativa y presenta recomendaciones de ahorro.**

## Funcionalidades

- Registro e inicio de sesión de usuarios.
- Análisis energético con diez variables de consumo y contexto del inmueble.
- Clasificación en **Eficiente**, **Moderado** o **Ineficiente** con el modelo SVC v2.0.0.
- Recomendaciones generadas por Gemini a partir de la categoría, el consumo, la cantidad de equipos y el costo estimado. Si Gemini no está disponible, el backend devuelve una recomendación de respaldo.
- Estimación del costo mensual en USD y conversión a moneda local mediante ExchangeRate-API (o tasas de desarrollo cuando no hay clave configurada).
- Estimación de emisiones eléctricas en kg CO₂ usando el factor del país y los datos de Ember 2024.
- Historial de análisis y reportes PDF.

La huella de carbono es una estimación, no una medición ni certificación. El indicador visual del frontend usa rangos orientativos, no umbrales regulatorios.

## Tecnologías y puertos

| Módulo | Tecnologías | Puerto local |
|---|---|---:|
| Frontend | React 19, Vite 8 | `5173` |
| Backend | Java 21, Spring Boot 4.1, Maven Wrapper | `8080` |
| Inferencia ML | Python 3.12, Flask, scikit-learn | `5000` |
| Base de datos | H2 en desarrollo; Oracle en producción | En memoria / externo |

El modo de desarrollo (`dev`) del backend se conecta al servicio Flask. En una ejecución local completa, inicia Flask antes del backend.

## Ejecución local

### Requisitos

- JDK 21 (el proyecto fija Java 21 en `backend/pom.xml`).
- Python 3.12 y `pip`.
- Node.js compatible con Vite 8 (Node.js 20.19+ o 22.12+).
- No hace falta instalar Maven globalmente: se incluye `backend/mvnw`.
- Claves de Gemini y ExchangeRate-API recomendadas para usar esos servicios en vivo; la aplicación dispone de respuestas/tasas de desarrollo cuando no se configuran.

Abre tres terminales desde la raíz del repositorio.

**1. Servicio de inferencia Flask**

```bash
cd datascience/inference_service
python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

El servicio escucha en `http://localhost:5000`; puede verificarse con `GET /health`.

**2. Backend Spring Boot**

```bash
cd backend
EXCHANGERATE_API_KEY='<TU KEY AQUI>' GEMINI_API_KEY='<TU KEY AQUI>' ./mvnw spring-boot:run  -Dspring-boot.run.profiles=dev   -DskipTests
```

El backend arranca también sin claves: ExchangeRate-API usa las tasas estáticas de desarrollo cuando no hay una clave configurada; Gemini agrega una recomendación local si la llamada no está disponible. Para habilitar ambas integraciones en vivo, exporta antes `EXCHANGERATE_API_KEY` y `GEMINI_API_KEY` con claves válidas.

El wrapper compila con Java 21 según el `pom.xml`. `clean` no es necesario en una clonación normal; úsalo si Maven intenta reutilizar clases compiladas con otra versión de Java:

```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests
```

El perfil `dev` usa H2 en memoria y apunta Flask a `localhost:5000`. Los datos de H2 se reinician al detener la aplicación.

**3. Frontend React + Vite**

```bash
cd frontend
npm install
cp .env.example .env.local
npm run dev
```

Abre **http://localhost:5173**. `VITE_API_URL` debe apuntar al backend, normalmente `http://localhost:8080`.

### Variables de entorno

| Variable | Uso | Comportamiento local |
|---|---|---|
| `GEMINI_API_KEY` | Recomendaciones de Gemini | Si la llamada falla, el backend agrega una recomendación de respaldo. |
| `EXCHANGERATE_API_KEY` | Tasas actuales desde ExchangeRate-API | Si falta o empieza con `dummy-`, usa tasas de desarrollo definidas en el backend. |
| `VITE_API_URL` | URL base del frontend para llamar al backend | `http://localhost:8080`. |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `dev` para H2 y Flask local. |

No publiques claves reales ni subas archivos `.env`/`.env.local`.

### Docker Compose

Desde la raíz, copia `.env.example` a `.env`, configura las variables y ejecuta:

```bash
cp .env.example .env
# Edita .env y agrega las claves que tengas disponibles.
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

En Compose, el frontend se sirve por **http://localhost/** en el puerto `80`; el backend se publica en `http://localhost:8080`. El servicio Flask escucha en el puerto `5000` dentro de la red de Docker. El puerto `5173` corresponde al servidor de desarrollo de Vite (`npm run dev`), no al frontend servido por Compose.

`VITE_API_URL` se incorpora durante la compilación de la imagen del frontend. Si cambias ese valor, vuelve a construirla. Para revisar logs o detener los contenedores:

```bash
docker compose logs --tail=100 servicio-data backend-springboot frontend-app
docker compose down
```

## API

La especificación de endpoints, campos, validaciones y ejemplos está en [`docs/contrato-api.md`](./docs/contrato-api.md). Con el backend iniciado, Swagger UI está disponible en `http://localhost:8080/swagger-ui/index.html`.

## Pruebas

```bash
(cd frontend && npm test)
(cd backend && ./mvnw test)
(cd datascience/inference_service && .venv/bin/python -m pytest)
```

## Documentación por módulo

- [`backend/README.md`](./backend/README.md): API Spring, configuración y persistencia.
- [`frontend/README.md`](./frontend/README.md): interfaz, variables de entorno y ejecución.
- [`datascience/README.md`](./datascience/README.md): modelo, datos e inferencia.
- [`docs/OCI.md`](./docs/OCI.md): despliegue en Oracle Cloud Infrastructure.

## Estructura

```
G9-LATAM-Team-42/
├── frontend/           → Interfaz React/Vite
├── backend/            → API Spring Boot y reglas de negocio
├── datascience/        → Modelo SVC, metadatos y factores de emisión
│   ├── inference_service/    → API Flask de inferencia
│   │   └──modelo_energia_v2.0.0.pkl   → Modelo
│   └── factor_emision_pais.csv        → Factores
├── docs/               → Contrato API y documentación de despliegue
├── docker-compose.yml  → Orquestación local/de despliegue
└── README.md
```


**G9 LATAM Team 42 · JouleAI**⚡
[![Demo en Vivo](https://img.shields.io/badge/DEMO-EN%20VIVO-brightgreen?style=for-the-badge&logo=googlechrome&logoColor=white)](http://129.80.145.212)
