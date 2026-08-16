# EnergiAI — Backend

EnergiAI es un servicio de backend especializado en el análisis de eficiencia energética. Procesa datos de consumo eléctrico para proporcionar a los usuarios un diagnóstico preciso sobre su eficiencia energética, integrando un modelo de Machine Learning y recomendaciones de IA generativa.

## Arquitectura

```
Frontend → Backend (Spring Boot) → Servicio Flask (ML) → Modelo Árbol de Decisión
                                  → Gemini API (Recomendaciones IA)
                                  → Base de Datos (Persistencia)
```

## Funcionalidades principales

- **Análisis Energético:** Recibe 10 campos de entrada (5 obligatorios + 5 opcionales), delega la predicción al modelo ML y retorna clasificación + recomendaciones + costo estimado.
- **Integración ML:** Se conecta al Servicio Flask de inferencia que ejecuta un modelo de Árbol de Decisión (scikit-learn) para clasificar eficiencia en 3 categorías: Eficiente, Moderado, Ineficiente.
- **Recomendaciones IA (Gemini):** Cuando la categoría es "Moderado" o "Ineficiente", consulta a Gemini API para generar una recomendación personalizada. Si Gemini falla, usa un mensaje fallback.
- **Estimación de Costos:** Calcula `costo_estimado = consumo_kwh * 0.75`.
- **Persistencia:** Almacena cada análisis en base de datos con todos los campos de entrada, resultado y recomendaciones.
- **Historial:** Endpoint GET para consultar análisis previos.
- **Validación:** Bean Validation en el DTO con rangos, patrones y campos obligatorios.

## Campos de entrada (AnalisisRequest)

| Campo | Tipo | Rango | Obligatorio | Default |
|-------|------|-------|-------------|---------|
| `consumo_kwh` | Double | 50–2000 | Sí | — |
| `tipo_inmueble` | String | Casa/Oficina/Apartamento/Comercio | Sí | — |
| `personas_vivienda` | Integer | 1–10 | Sí | — |
| `cantidad_equipos` | Integer | 1–20 | Sí | — |
| `horas_alto_consumo` | Double | 0.0–24.0 | Sí | — |
| `uso_horario_pico` | Integer | 0, 1 | No | 0 |
| `antiguedad_inmueble` | Integer | 2–31 | No | 10 |
| `tiene_aire_acondicionado` | Integer | 0, 1 | No | 0 |
| `tiene_calentador_electrico` | Integer | 0, 1 | No | 0 |
| `electrodomesticos_eficientes` | Integer | 0, 1 | No | 0 |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/analisis-energetico` | Procesar análisis de eficiencia energética |
| GET | `/analisis-energetico/historial` | Obtener historial de análisis |

## Tecnologías

- **Framework:** Spring Boot 4.1.0, Java 21
- **Persistencia:** Spring Data JPA, Hibernate 7.4, Flyway
- **Base de datos:** H2 (dev) / Oracle Autonomous DB (prod)
- **Seguridad:** Spring Security (autenticación básica)
- **Documentación API:** SpringDoc OpenAPI 3 (Swagger UI)
- **Testing:** JUnit 5, Mockito, jqwik (property-based testing)
- **Build:** Maven

## Perfiles

| Perfil | DataScienceClient | Base de datos | Configuración |
|--------|-------------------|---------------|---------------|
| `dev` | MockDataScienceClient (heurística local) | H2 en memoria | application-dev.properties |
| `prod` | RestDataScienceClient (HTTP → Flask) | Oracle (OCI) | application-prod.properties |

## Estructura del proyecto

```
backend/src/main/java/EnergiAI/demo/
├── DemoApplication.java
├── client/
│   ├── DataScienceClient.java          (interface)
│   ├── MockDataScienceClient.java      (perfil dev)
│   ├── RestDataScienceClient.java      (perfil prod)
│   └── GeminiClient.java              (recomendaciones IA)
├── controller/
│   └── AnalisisController.java
├── dto/
│   ├── AnalisisRequest.java
│   ├── AnalisisResponse.java
│   └── PrediccionResponse.java
├── exception/
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── model/
│   └── AnalisisEnergetico.java
├── repository/
│   └── AnalisisEnergeticoRepository.java
├── security/
│   └── SecurityConfig.java
└── service/
    └── AnalisisService.java
```
