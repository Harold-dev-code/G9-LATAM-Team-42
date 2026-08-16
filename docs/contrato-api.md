# Contrato API: Backend ↔ Servicio Flask de Predicción

> [!IMPORTANT]
> Este documento define la estructura exacta de comunicación entre el **Backend (Spring Boot / Java)** y el **Servicio Flask de Inferencia ML (Python)**.
> Cualquier cambio en los campos, tipos o rangos debe reflejarse aquí primero.

---

## POST `/predict` — Predicción de Eficiencia Energética

### Request (Backend → Flask)

El Backend envía un JSON con los **10 campos del usuario** (5 obligatorios + 5 opcionales).

| Campo | Tipo | Rango / Valores | Obligatorio | Default |
|-------|------|-----------------|-------------|---------|
| `consumo_kwh` | Float | 50 – 2000 | ✅ Sí | — |
| `tipo_inmueble` | String | `"Casa"`, `"Oficina"`, `"Apartamento"`, `"Comercio"` | ✅ Sí | — |
| `personas_vivienda` | Integer | 1 – 10 | ✅ Sí | — |
| `cantidad_equipos` | Integer | 1 – 20 | ✅ Sí | — |
| `horas_alto_consumo` | Float | 0.0 – 24.0 | ✅ Sí | — |
| `uso_horario_pico` | Integer | 0 ó 1 | ❌ No | 0 |
| `antiguedad_inmueble` | Integer | 2 – 31 | ❌ No | 10 |
| `tiene_aire_acondicionado` | Integer | 0 ó 1 | ❌ No | 0 |
| `tiene_calentador_electrico` | Integer | 0 ó 1 | ❌ No | 0 |
| `electrodomesticos_eficientes` | Integer | 0 ó 1 | ❌ No | 0 |

**Ejemplo de request:**

```json
{
    "consumo_kwh": 350.0,
    "tipo_inmueble": "Casa",
    "personas_vivienda": 4,
    "cantidad_equipos": 8,
    "horas_alto_consumo": 5.0,
    "uso_horario_pico": 1,
    "antiguedad_inmueble": 15,
    "tiene_aire_acondicionado": 0,
    "tiene_calentador_electrico": 1,
    "electrodomesticos_eficientes": 0
}
```

### Response 200 (Flask → Backend)

| Campo | Tipo | Valores posibles |
|-------|------|------------------|
| `categoria` | String | `"Eficiente"`, `"Moderado"`, `"Ineficiente"` |
| `probabilidad` | Float | 0.0 – 1.0 |

```json
{
    "categoria": "Moderado",
    "probabilidad": 0.72
}
```

> **Nota:** `probabilidad` representa la confianza del modelo en la categoría asignada (valor decimal entre 0 y 1).

### Response 400 — Error de Validación

Cuando uno o más campos son inválidos o están ausentes.

```json
{
    "error": "Validation failed",
    "details": [
        "El campo 'consumo_kwh' es obligatorio",
        "El valor de 'personas_vivienda' debe estar entre 1 y 10"
    ]
}
```

---

## GET `/health` — Estado del Servicio

### Response 200

```json
{
    "status": "ok",
    "model_loaded": true
}
```

---

## Features Calculados (Internos de Flask)

Los siguientes campos son calculados internamente por el Servicio Flask y **NO deben ser enviados por el Backend**:

| Campo | Fórmula |
|-------|---------|
| `consumo_promedio_diario` | `consumo_kwh / 30` |
| `ratio_persona_kwh` | `consumo_kwh / personas_vivienda` |
| `consumo_por_equipo` | `consumo_kwh / cantidad_equipos` |
| `consumo_por_hora_pico` | `consumo_kwh / horas_alto_consumo` (0 si horas == 0) |
| `costo_estimado_mensual` | `consumo_kwh * 0.75` |

Estos campos existen porque el modelo ML fue entrenado con 15 features. El Servicio Flask los calcula antes de invocar `model.predict()`.

---

## Configuración de Conexión

| Parámetro | Valor |
|-----------|-------|
| URL (producción) | `http://python-service:5000` |
| Timeout de conexión | 5000 ms |
| Timeout de lectura | 10000 ms |

---

## Notas de Integración

- El Backend valida los campos antes de enviarlos a Flask (fail-fast para el usuario).
- Flask aplica una segunda validación como línea de defensa antes del modelo.
- Los campos opcionales con valor `null` en el Backend se envían con su default antes de la llamada a Flask.
- La serialización usa `snake_case` para todos los campos JSON.
