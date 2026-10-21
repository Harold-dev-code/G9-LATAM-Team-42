# Contrato API de JouleAI

Este documento describe la comunicación entre el frontend, el backend Spring Boot y el servicio de inferencia Flask. Las claves API y los datos de entorno no forman parte de las solicitudes HTTP del navegador.

## Servicios y puertos locales

| Servicio | URL local | Uso |
|---|---|---|
| Frontend (Vite) | `http://localhost:5173` | Interfaz de desarrollo |
| Backend (Spring Boot) | `http://localhost:8080` | API pública para la aplicación |
| Servicio Flask | `http://localhost:5000` | Inferencia interna llamada por Spring |

## Autenticación

### `POST /auth/register`

Registra un usuario.

```json
{
  "email": "ana@example.com",
  "password": "secreto123",
  "nombre": "Ana"
}
```

`email`, `password` y `nombre` son obligatorios; la contraseña debe tener al menos seis caracteres. La respuesta incluye `userId`, `email`, `nombre` y `message`.

### `POST /auth/login`

```json
{
  "email": "ana@example.com",
  "password": "secreto123"
}
```

La respuesta tiene el mismo esquema de usuario que el registro. El frontend usa `userId` en la cabecera `X-User-Id` para asociar análisis e historial.

## Análisis energético

### `POST /analisis-energetico`

Recibe un análisis mensual, lo envía a Flask para clasificación, obtiene una recomendación breve de Gemini (con respaldo local si no está disponible), calcula costo y huella, y guarda el análisis asociado al inmueble y al usuario. La huella se devuelve en esta respuesta, pero no se persiste en el registro del historial.

Cabecera usada por el frontend:

```http
X-User-Id: 123
```

Esquema del cuerpo:

| Campo | Tipo | Requerido | Validación / valor predeterminado |
|---|---|---:|---|
| `consumo_kwh` | número | Sí | 50–2000 kWh/mes |
| `tipo_inmueble` | texto | Sí | `Casa`, `Oficina`, `Apartamento` o `Comercio` |
| `pais` | texto | Sí | País del selector de la interfaz |
| `moneda` | texto | Sí | Código de moneda local |
| `periodo` | texto | Sí | Período del análisis; la interfaz envía `mensual` |
| `etiqueta` | texto | Sí | `Residencial` o `Comercial` |
| `personas_vivienda` | entero | Sí | 1–10 |
| `cantidad_equipos` | entero | Sí | 1–20 |
| `horas_alto_consumo` | número | Sí | 0–24 |
| `nombre_inmueble` | texto | Sí | No vacío |
| `direccion_inmueble` | texto | No | Dirección del inmueble |
| `mes_facturado` | entero | Sí | 1–12 |
| `anio_facturado` | entero | Sí | 2000–2100 |
| `uso_horario_pico` | entero | No | `0` o `1`; valor predeterminado `0` |
| `antiguedad_inmueble` | entero | No | Valor predeterminado `10`; el servicio de inferencia acepta 2–31 |
| `tiene_aire_acondicionado` | entero | No | `0` o `1`; valor predeterminado `0` |
| `tiene_calentador_electrico` | entero | No | `0` o `1`; valor predeterminado `0` |
| `electrodomesticos_eficientes` | entero | No | `0` o `1`; valor predeterminado `0` |
| `tarifa_kwh` | número | No | Tarifa en USD/kWh; valor predeterminado `0.75` |

El frontend ofrece estos 15 países y monedas:

| País | Moneda |
|---|---|
| Colombia | COP |
| México | MXN |
| Chile | CLP |
| Argentina | ARS |
| Perú | PEN |
| Brasil | BRL |
| Uruguay | UYU |
| Ecuador | USD |
| Guatemala | GTQ |
| Honduras | HNL |
| Paraguay | PYG |
| Bolivia | BOB |
| Costa Rica | CRC |
| República Dominicana | DOP |
| Venezuela | VES |

Ejemplo de solicitud:

```json
{
  "consumo_kwh": 350,
  "tipo_inmueble": "Casa",
  "pais": "Colombia",
  "moneda": "COP",
  "periodo": "mensual",
  "etiqueta": "Residencial",
  "personas_vivienda": 4,
  "cantidad_equipos": 8,
  "horas_alto_consumo": 5,
  "uso_horario_pico": 1,
  "antiguedad_inmueble": 15,
  "tiene_aire_acondicionado": 0,
  "tiene_calentador_electrico": 1,
  "electrodomesticos_eficientes": 0,
  "tarifa_kwh": 0.75,
  "nombre_inmueble": "Casa principal",
  "direccion_inmueble": "Calle 10 # 20-30",
  "mes_facturado": 9,
  "anio_facturado": 2026
}
```

### Respuesta `200 OK`

```json
{
  "categoria": "Moderado",
  "probabilidad": 0.82,
  "recomendaciones": [
    "RECOMENDACION: ¡Hola! Considera distribuir el uso de tus 8 equipos para ahorrar energía."
  ],
  "costo_estimado": 262.5,
  "costo_estimado_usd": 262.5,
  "costo_estimado_local": 1102500.0,
  "pais": "Colombia",
  "moneda": "COP",
  "huella_carbono_kg": 104.3,
  "factor_emision_kg_co2_kwh": 0.298,
  "anio_factor_emision": 2024,
  "fuente_factor_emision": "Ember Yearly Electricity Data"
}
```

`probabilidad` está entre 0 y 1. `costo_estimado` y `costo_estimado_usd` son el costo calculado con la tarifa en USD; `costo_estimado_local` usa la tasa del país y puede variar según ExchangeRate-API o las tasas locales de desarrollo. El backend determina la moneda local a partir del país.

La huella se calcula como `consumo_kwh × factor_emision_kg_co2_kwh`, redondeada a dos decimales. Los factores de la fuente Ember Yearly Electricity Data corresponden a 2024 y se distribuyen bajo CC BY 4.0; los valores usados están documentados en [`../datascience/factor_emision_pais.csv`](../datascience/factor_emision_pais.csv). No son una medición individual de la factura. Si el país no tiene factor en el backend, `huella_carbono_kg`, `factor_emision_kg_co2_kwh`, `anio_factor_emision` y `fuente_factor_emision` son `null`.

Si ExchangeRate-API no tiene clave configurada (vacía o `dummy-*`), el servicio usa tasas estáticas locales. Si una llamada con clave configurada falla, el backend continúa con una tasa predeterminada `1.0`; en ese caso el monto local puede coincidir con el monto en USD.

La barra del frontend escala visualmente de 0 a 500 kg CO₂/mes y clasifica bajo (hasta 100), moderado (más de 100 y hasta 250) o alto (más de 250). Son niveles ilustrativos, no umbrales regulatorios.

### Errores

- `400 Bad Request`: solicitud inválida, integración externa fallida u otro error de negocio capturado por el manejador global. El cuerpo incluye `timestamp`, `status`, `error`, `messages` y `path`.
- `401 Unauthorized`: no se pudo asociar el análisis con un usuario válido.
- `409 Conflict`: ya existe un análisis para el mismo inmueble, mes y año.

## Historial

### `GET /analisis-energetico/historial`

Envía `X-User-Id` para consultar el historial del usuario. Sin esa cabecera, el servicio actual devuelve una lista vacía.

Cada elemento usa camelCase e incluye:

```json
{
  "id": 12,
  "mesFacturado": 9,
  "anioFacturado": 2026,
  "consumoKwh": 350.0,
  "categoria": "Moderado",
  "probabilidad": 0.82,
  "costoEstimadoMensual": 262.5,
  "tarifaKwh": 0.75,
  "recomendaciones": "Reducir el uso de equipos durante el horario pico",
  "fechaCreacion": "2026-10-20T14:30:00",
  "nombreInmueble": "Casa principal",
  "direccionInmueble": "Calle 10 # 20-30"
}
```

El DTO del historial todavía no incluye la huella de carbono.

### `DELETE /analisis-energetico/{id}`

Elimina el análisis indicado. Respuesta exitosa: `204 No Content`.

## API interna de inferencia Flask

El backend llama al microservicio en `http://localhost:5000` durante desarrollo y en `http://python-service:5000` dentro de Docker.

### `POST /predict`

Flask recibe las diez variables del modelo, no los datos de autenticación, país, moneda ni inmueble: `consumo_kwh`, `tipo_inmueble`, `personas_vivienda`, `cantidad_equipos`, `horas_alto_consumo`, `uso_horario_pico`, `antiguedad_inmueble`, `tiene_aire_acondicionado`, `tiene_calentador_electrico` y `electrodomesticos_eficientes`.

Los cinco primeros campos son obligatorios. Los opcionales usan valores `0`, excepto `antiguedad_inmueble`, cuyo valor predeterminado es `10`. La API valida consumo 50–2000, categorías de inmueble descritas arriba, personas 1–10, equipos 1–20, horas 0–24, antigüedad 2–31 y variables binarias `0`/`1`.

Flask devuelve únicamente la clasificación y la probabilidad de la clase predicha:

```json
{
  "categoria": "Moderado",
  "probabilidad": 0.82
}
```

El modelo usa esas diez características originales; el pipeline serializado aplica codificación y escalado. El cálculo de costo, Gemini, conversión monetaria y huella de carbono pertenecen al backend, no a Flask.

### `GET /health`

```json
{
  "status": "ok",
  "model_loaded": true
}
```

## Integraciones de terceros

| Integración | Configuración | Uso |
|---|---|---|
| Gemini | `GEMINI_API_KEY` | Una recomendación breve: el prompt transmite categoría, consumo, cantidad de equipos y costo mensual; pide un consejo directo y amigable que inicia con `RECOMENDACION: ¡Hola!`. Gemini ya no genera tablas ni conversiones de moneda. |
| ExchangeRate-API | `EXCHANGERATE_API_KEY` | Tasas de cambio actuales con USD como base; el backend selecciona las monedas de LATAM que utiliza. |
| Tasas de desarrollo | No requiere clave válida; se activa con clave vacía o `dummy-*` | Valores locales de respaldo para desarrollo, no tasas financieras en tiempo real. |

## Variables locales del backend

```properties
GEMINI_API_KEY=<clave-de-gemini>
EXCHANGERATE_API_KEY=<clave-de-exchangerate>
SPRING_PROFILES_ACTIVE=dev
```

No incluyas claves reales en el repositorio. Para configurar todos los servicios localmente, sigue [`../README.md`](../README.md).
