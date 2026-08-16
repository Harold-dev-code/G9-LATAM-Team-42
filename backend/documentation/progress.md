# Progreso del Proyecto — Backend EnergiAI

Registro de los hitos y cambios realizados en el desarrollo del backend.

## Hitos Completados

### Fase 1: Core de Análisis
- [x] Implementación de `AnalisisRequest` y `AnalisisResponse` (DTOs)
- [x] Implementación de `AnalisisService` con lógica de clasificación y recomendaciones
- [x] Implementación de `AnalisisController` con endpoint `POST /analisis-energetico`

### Fase 2: Persistencia e Historial
- [x] Creación de la entidad JPA `AnalisisEnergetico`
- [x] Creación del repositorio `AnalisisEnergeticoRepository`
- [x] Integración de la persistencia en `AnalisisService`
- [x] Implementación del endpoint `GET /analisis-energetico/historial`
- [x] Configuración de H2 (dev) y Oracle (prod) en profiles separados

### Fase 3: Desacoplamiento de Ciencia de Datos
- [x] Interface `DataScienceClient`
- [x] `MockDataScienceClient` (perfil dev) — heurística basada en puntaje
- [x] `RestDataScienceClient` (perfil prod) — HTTP hacia Flask con timeouts

### Fase 4: Manejo de Errores y Seguridad
- [x] `GlobalExceptionHandler` para manejo centralizado de excepciones
- [x] `SecurityConfig` con autenticación básica
- [x] Documentación OpenAPI con SpringDoc/Swagger

### Fase 5: Integración ML (16-ago-2026)
- [x] `AnalisisRequest` ampliado a 10 campos (5 obligatorios + 5 opcionales)
- [x] Bean Validation con @Min/@Max/@Pattern/@DecimalMin/@DecimalMax
- [x] `AnalisisEnergetico` entidad actualizada con nuevos campos
- [x] `PrediccionResponse` con no-args constructor para Jackson
- [x] `MockDataScienceClient` reescrito con heurística de 10 campos (score 0-100)
- [x] `RestDataScienceClient` con RestClient, timeout de conexión (5s) y lectura (10s)
- [x] `AnalisisService` actualizado:
  - Defaults para campos opcionales nulos
  - Costo estimado: `consumo_kwh * 0.75`
  - Gemini solo se invoca si categoría ≠ "Eficiente"
  - Fallback genérico si Gemini falla
  - Persistencia de todos los campos nuevos
- [x] Integración con GeminiClient (API Gemini 3.5 Flash)
- [x] Configuración `application-prod.properties` con URL y timeouts de Flask

### Fase 6: Property-Based Testing (16-ago-2026)
- [x] Dependencia `jqwik 1.9.2` agregada al `pom.xml`
- [x] `MockDataScienceClientPropertyTest` — 4 propiedades (200 tries c/u)
  - Categoría siempre es una de 3 válidas
  - Probabilidad siempre en [0.0, 1.0]
  - Respuesta nunca es null
  - Campos opcionales nulos no causan errores
- [x] `AnalisisServicePropertyTest` — 2 propiedades (200 tries c/u)
  - Defaults aplicados correctamente a campos nulos
  - Costo estimado = consumo_kwh * 0.75
- [x] `AnalisisControllerPropertyTest` — 8 propiedades (100-200 tries c/u)
  - Request válido no produce violaciones
  - Cada campo obligatorio null produce violación
  - Campos fuera de rango producen violación
  - Tipo inmueble inválido produce violación
- [x] Tests unitarios existentes actualizados al nuevo DTO de 10 campos
- [x] Fix: `gemini.api.key` con default para tests (`${GEMINI_API_KEY:dummy-key-for-tests}`)
- [x] `maven-surefire-plugin` configurado para detectar `*PropertyTest.java`

## Estado actual de tests

| Tipo | Cantidad | Estado |
|------|----------|--------|
| Unit Tests (JUnit + Mockito) | 6 | ✅ Pasando |
| Property Tests (jqwik) | 14 | ✅ Pasando |
| **Total** | **20** | **✅ 0 errores** |

## Siguientes Pasos
- [ ] Dockerizar el backend para despliegue junto con Flask
- [ ] Configurar CI/CD con GitHub Actions
- [ ] Test de integración E2E (Backend + Flask + H2)
- [ ] Migrar de H2 a Oracle Autonomous Database (OCI Always Free) en producción
