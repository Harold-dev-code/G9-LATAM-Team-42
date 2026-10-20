# Backend JouleAI

API REST de JouleAI construida con **Java 21**, **Spring Boot 4.1** y Maven. El backend valida y registra análisis, consulta el servicio Flask, obtiene recomendaciones de Gemini, convierte el costo a moneda local y estima la huella de carbono.

## Requisitos y ejecución local

- JDK 21. La versión de compilación está fijada como `java.version=21` en `pom.xml`.
- Python 3.12 para el servicio de inferencia Flask, si se usa el perfil normal `dev`.
- Maven es opcional: el repositorio incluye el Maven Wrapper `mvnw`.

Desde la raíz inicia primero Flask (ver [`../datascience/README.md`](../datascience/README.md)) en el puerto `5000`. Después, en otra terminal:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests
```

La API queda en `http://localhost:8080`. El perfil `dev` usa H2 en memoria (`jdbc:h2:mem:testdb`) y conecta con Flask en `http://localhost:5000`. H2 se reinicia al detener la aplicación. Swagger UI: `http://localhost:8080/swagger-ui/index.html`; consola H2: `http://localhost:8080/h2-console`.

El backend puede arrancar sin claves reales. Si no se define `EXCHANGERATE_API_KEY`, las tasas de conversión estáticas de desarrollo se usan automáticamente; si Gemini no está disponible, el análisis incluye una recomendación de respaldo. Para llamadas reales, exporta `EXCHANGERATE_API_KEY` y `GEMINI_API_KEY` con credenciales válidas antes de iniciar Maven.

Si se presenta `UnsupportedClassVersionError` por clases compiladas con otro JDK, limpia los artefactos y vuelve a ejecutar:

```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests
```

No es necesario ejecutar `clean` en cada inicio ni después de un clon normal, porque `target/` está excluido del repositorio.

## Integraciones y configuración

| Variable | Propiedad | Propósito |
|---|---|---|
| `GEMINI_API_KEY` | `gemini.api.key` | Gemini API para una recomendación breve basada en categoría, consumo, cantidad de equipos y costo mensual. Si no responde, se añade una recomendación local de respaldo. |
| `EXCHANGERATE_API_KEY` | `exchangerate.api.key` | ExchangeRate-API para tasas actuales con USD como base. Si la clave es nula, vacía o `dummy-`, se usan tasas estáticas de desarrollo. |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | `dev` por defecto; `dev` usa H2 y Flask local. |
| `DB_HOST`, `DB_PORT`, `DB_SERVICE_NAME` | `application-prod.properties` | Conexión Oracle en producción. |
| `DB_USER`, `DB_PASSWORD` | `application-prod.properties` | Credenciales Oracle. |
| `PUBLIC_IP` | CORS | Origen frontend adicional permitido; por defecto `http://localhost`. |

Los archivos de configuración usan claves de ejemplo `dummy-key-for-dev`. No sirven para hacer llamadas autenticadas a Gemini o ExchangeRate-API. Si no se configura una clave (o queda vacía/`dummy-*`), Gemini agrega el mensaje de respaldo y el conversor usa las tasas estáticas de desarrollo. Si ExchangeRate-API falla con una clave configurada, el backend continúa con la tasa predeterminada `1.0`, por lo que el valor local puede coincidir con el monto en USD. Guarda las claves reales en variables locales o en un gestor de secretos, nunca en el repositorio.

El frontend ofrece **15 países**: Colombia, México, Chile, Argentina, Perú, Brasil, Uruguay, Ecuador, Guatemala, Honduras, Paraguay, Bolivia, Costa Rica, República Dominicana y Venezuela. Las tasas de conversión actuales provienen de ExchangeRate-API cuando hay clave válida.

El cálculo de huella usa factores de intensidad de electricidad 2024 de Ember (CC BY 4.0), documentados en `../datascience/factor_emision_pais.csv` y disponibles en el servicio backend. Se calcula `consumo_kwh × factor_kg_co2_kwh`; la respuesta del análisis devuelve estimación, factor, año y fuente. La huella aún no forma parte del DTO de historial.

## Endpoints

El contrato completo de solicitudes, validaciones y respuestas está en [`../docs/contrato-api.md`](../docs/contrato-api.md).

| Método | Ruta | Función |
|---|---|---|
| `POST` | `/auth/register` | Registrar usuario |
| `POST` | `/auth/login` | Iniciar sesión |
| `POST` | `/analisis-energetico` | Ejecutar y guardar un análisis (requiere `X-User-Id` para asociarlo a un usuario) |
| `GET` | `/analisis-energetico/historial` | Consultar análisis del usuario indicado por `X-User-Id` |
| `DELETE` | `/analisis-energetico/{id}` | Eliminar un análisis |

El análisis recibe variables del consumo, del inmueble, del período, el país y la tarifa. El backend envía al modelo solo las diez variables requeridas por el SVC: consumo, tipo de inmueble, personas, equipos, horas de alto consumo, uso en hora pico, antigüedad y tres indicadores de equipamiento/eficiencia.

## Persistencia y pruebas

- `dev`: H2 en memoria.
- `prod`: Oracle Database, configurada mediante `application-prod.properties`.
- JPA actualiza el esquema según las entidades (`spring.jpa.hibernate.ddl-auto=update`).
- El historial persiste análisis, datos del inmueble y resultados energéticos. La respuesta de historial usa propiedades camelCase y no incluye actualmente la huella nueva.

Ejecutar las pruebas:

```bash
cd backend
./mvnw test
```

Para ejecutar pruebas omitiendo compilación de tests al iniciar el servicio:

```bash
cd backend
EXCHANGERATE_API_KEY='<TU KEY AQUI>' GEMINI_API_KEY='<TU KEY AQUI>' ./mvnw spring-boot:run  -Dspring-boot.run.profiles=dev   -DskipTests
```
