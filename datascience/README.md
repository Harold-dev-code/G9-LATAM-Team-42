# JouleAI — Proceso de Ciencia de Datos

**G9 LATAM Team 42 · Hackathon**

**Objetivo:** construir una solución inteligente capaz de analizar patrones de consumo energético, clasificar perfiles de eficiencia, generar recomendaciones, estimar costos y dejar el resultado listo para integración mediante API REST.

**Modelo actual de producción:** SVC (SVM con kernel RBF), versión 2.0.0. El pipeline serializado incluye codificación y escalado.

---

## 1. Problema y objetivo del MVP

Muchas personas y pequeños negocios reciben facturas elevadas de energía, pero no saben con claridad qué hábitos están generando ese consumo ni qué acciones deberían priorizar.

La solución busca:

- **Clasificar** el perfil energético en:
  - Eficiente
  - Moderado
  - Ineficiente
- **Recomendar** acciones para reducir desperdicios.
- **Estimar costos** usando una tarifa de referencia de **$0.75 por kWh**.
- **Integrar** los resultados mediante una respuesta JSON compatible con una API REST.

> **Interpretación:** el objetivo del Data Science no es solo predecir una categoría. También debe transformar los datos en información útil, explicable y accionable.

---

## 2. Datos utilizados

La base utilizada contiene:

- **5,000 registros**
- **14 variables originales**
- **10 variables utilizadas por el modelo de producción**

### Variables del endpoint

El modelo final utiliza únicamente las variables que realmente estarán disponibles en la API:

- `consumo_kwh`
- `uso_horario_pico`
- `cantidad_equipos`
- `tipo_inmueble`
- `horas_alto_consumo`

> **Interpretación:** esta decisión evita entrenar con variables que después no estarán disponibles cuando el usuario utilice la aplicación.

---

## 3. Flujo completo de Ciencia de Datos

El proceso desarrollado sigue las siguientes etapas:

1. Comprensión del problema.
2. Carga y revisión del dataset.
3. Análisis de calidad.
4. Análisis exploratorio de datos (EDA).
5. Revisión de valores perdidos.
6. Revisión de duplicados.
7. Validación de dominios.
8. Análisis de outliers.
9. Auditoría del target y detección de leakage.
10. Construcción de una nueva etiqueta energética.
11. Selección de variables.
12. Separación Train / Validation / Test.
13. Preprocesamiento.
14. Creación de baseline.
15. Comparación de 8 modelos.
16. Selección del modelo de producción.
17. Calibración de probabilidades.
18. Evaluación final.
19. Interpretabilidad.
20. Recomendaciones.
21. Estimación financiera.
22. Respuesta JSON.
23. Serialización.
24. Preparación para OCI.

> **Interpretación:** el modelo es solo una parte del proceso. El valor del proyecto está en que todas las decisiones anteriores y posteriores al entrenamiento quedan documentadas.

---

## 4. Calidad de datos

Antes de entrenar cualquier modelo se evaluó la calidad de la información.

### Resultados

- **Valores perdidos:** 0
- **Duplicados:** 0
- **Filas físicamente inválidas:** 0

Se validaron condiciones como:

- consumo no negativo;
- horas de alto consumo entre 0 y 24;
- cantidad de equipos no negativa;
- variables binarias con valores válidos;
- antigüedad del inmueble no negativa.

> **Interpretación:** que el dataset tenga 0 nulos no significa que este paso se deba omitir. La calidad debe verificarse antes de entrenar.

---

## 5. Análisis exploratorio de datos — EDA

El EDA permitió comprender cómo se comporta el consumo según diferentes tipos de inmueble.

Se analizaron:

- distribuciones;
- estadísticos descriptivos;
- consumo por tipo de inmueble;
- horas de alto consumo;
- cantidad de equipos;
- patrones de horario pico.

### Hallazgo principal

El consumo energético no debe interpretarse de la misma forma para:

- apartamento;
- casa;
- oficina;
- comercio.

Los comercios y oficinas presentan referencias de consumo mayores que los apartamentos.

> **Interpretación:** por eso `tipo_inmueble` es una variable importante. Un mismo nivel de kWh puede ser normal para un comercio y elevado para una vivienda pequeña.

---

## 6. Análisis de outliers

Los valores atípicos se detectaron usando el método del **rango intercuartílico (IQR)**.

### Ejemplo

En `consumo_kwh` se detectaron aproximadamente **103 outliers**.

Sin embargo, no fueron eliminados automáticamente.

### Decisión del equipo

Solo se eliminan valores físicamente imposibles o claramente erróneos.

No se eliminan consumos altos simplemente por estar alejados de la distribución.

> **Interpretación:** en un problema energético, un consumo extremadamente alto puede ser precisamente el comportamiento ineficiente que queremos detectar.

---

## 7. Auditoría del target y detección de Data Leakage

Antes de entrenar se evaluó la variable objetivo original.

Se encontró que `categoria_eficiencia` podía ser reconstruida prácticamente de forma directa usando `ratio_persona_kwh`.

### Resultado

**Coincidencia de la regla simple ratio → categoría: 100%.**

### Problema

Si utilizábamos directamente ese target, los modelos podían obtener métricas muy altas sin realmente aprender un patrón complejo.

Eso constituye un riesgo de **data leakage**.

### Solución

Se creó una nueva pseudo-etiqueta energética basada en diferentes factores:

- consumo relativo;
- horas de alto consumo;
- horario pico;
- cantidad de equipos;
- contexto energético.

> **Interpretación:** esta decisión evita métricas artificialmente perfectas y hace el proyecto más defendible metodológicamente.

---

## 8. Nueva etiqueta energética

Se definieron tres categorías:

- **Eficiente**
- **Moderado**
- **Ineficiente**

El criterio interno utiliza un score basado en:

| Componente | Peso aproximado |
|---|---:|
| Consumo relativo | 35 puntos |
| Horas de alto consumo | 20 puntos |
| Uso en horario pico | 10 puntos |
| Cantidad de equipos | 15 puntos |
| Contexto energético | Ajuste |

> **Interpretación:** esta etiqueta es un criterio del MVP, no una norma universal. En una solución real debería recalibrarse con datos reales y criterios técnicos validados.

---

## 9. Separación de datos

El dataset se dividió de forma estratificada:

- **70% Train:** 3,500 registros
- **15% Validation:** 750 registros
- **15% Test:** 750 registros

### Objetivo de cada conjunto

**Train:** entrenar los modelos.

**Validation:** comparar y revisar calibración.

**Test:** evaluar el modelo final una sola vez.

> **Interpretación:** Test se mantiene aislado para evitar seleccionar el modelo basándonos en datos que deberían representar situaciones nuevas.

---

## 10. Preprocesamiento

El preprocesamiento se encapsuló dentro de un `Pipeline`.

### Variables numéricas

Se aplica:

- imputación con mediana;
- escalado.

### Variable categórica

Para `tipo_inmueble` se utiliza:

- imputación con el valor más frecuente;
- One-Hot Encoding.

> **Interpretación:** el Pipeline garantiza que el mismo tratamiento utilizado durante el entrenamiento se aplique después en producción.

---

## 11. Baseline

Antes de comparar modelos se creó un modelo baseline con `DummyClassifier`.

El baseline predice siempre la clase más frecuente.

Su objetivo es establecer un punto mínimo de comparación.

> **Interpretación:** un modelo de Machine Learning solo tiene sentido si supera de forma clara una estrategia extremadamente sencilla.

---

## 12. Métrica principal

La métrica principal definida fue **F1 Macro**.

También se analizaron:

- Accuracy;
- Balanced Accuracy;
- Precision Macro;
- Recall Macro;
- Log Loss.

### ¿Por qué F1 Macro?

Porque queremos que el modelo funcione de manera equilibrada para las tres categorías.

Si utilizáramos únicamente Accuracy, una clase mayoritaria podría ocultar un mal desempeño en las categorías con menos registros.

> **Interpretación:** la métrica se definió antes de conocer el ganador, evitando elegir el criterio después de observar los resultados.

---

## 13. Modelo de producción

El modelo activo es SVC con kernel RBF. Para esta versión, las métricas de producción se toman de `modelo_energia_v2.0.0_metadata.json`, que acompaña al artefacto serializado. Los experimentos exploratorios del notebook no reemplazan esos metadatos como referencia del artefacto desplegado.

---

## 14. Modelo seleccionado para producción

El artefacto desplegado es `modelo_energia_v2.0.0.pkl`, identificado en sus metadatos como un clasificador **SVC** (SVM con kernel RBF). El pipeline serializado ejecuta One-Hot Encoding para `tipo_inmueble` y `StandardScaler` para las variables numéricas; Flask envía las 10 variables originales y no vuelve a escalar los datos.

Las métricas registradas para esta versión son F1 Macro de **0.9599** en validación y **0.9502** en test. El metadata también registra `usa_escalado: true` y un hash SHA-256 para comprobar que el artefacto corresponde a esos metadatos.

---

## 15. Probabilidades

El SVC se serializó con `probability=True`, por lo que `predict_proba()` devuelve las probabilidades de clase consumidas por la API. Los metadatos no registran una calibración externa adicional.

---

## 16. Evaluación final en Test

El SVC v2.0.0 fue evaluado con los conjuntos de validación y test registrados en metadata: 1,000 filas en cada conjunto.

### Métricas

| Métrica | Validación | Test |
|---|---:|---:|
| Accuracy | **0.960** | **0.950** |
| F1 Macro | **0.9599** | **0.9502** |
| F1 Weighted | **0.9601** | **0.9504** |
| Balanced Accuracy | **0.9599** | **0.9501** |
| MCC | **0.9400** | **0.9255** |

Estas cifras corresponden al artefacto y hash declarados en los metadatos.

---

## 17. Interpretabilidad del modelo

Se utilizó **Permutation Importance** para determinar qué variables influyen más en la predicción.

### Variables más relevantes

- `consumo_kwh`
- `tipo_inmueble`
- `horas_alto_consumo`
- `uso_horario_pico`
- `cantidad_equipos`

### Hallazgo principal

`consumo_kwh` y `tipo_inmueble` son especialmente relevantes.

> **Interpretación:** esto tiene sentido desde el negocio: el consumo energético debe analizarse dentro del contexto del inmueble.

---

## 18. Motor de recomendaciones

Las recomendaciones no son iguales para todos los usuarios.

Se utilizan:

- categoría predicha;
- consumo observado;
- percentiles por tipo de inmueble;
- horario pico;
- cantidad de equipos;
- horas de alto consumo.

### Ejemplos

- reducir actividades durante horario pico;
- distribuir actividades de alto consumo;
- revisar equipos que permanecen encendidos;
- priorizar equipos de mayor eficiencia.

> **Interpretación:** el objetivo es convertir la predicción del modelo en acciones concretas.

---

## 19. Estimación financiera

Se utiliza la tarifa de referencia:

**$0.75 por kWh**

### Ejemplo

Para:

`consumo_kwh = 420`

el costo estimado es:

**420 × 0.75 = $315.00 mensuales**

Además, el sistema puede simular escenarios de reducción de:

- 5%;
- 10%;
- 15%.

> **Interpretación:** esto permite comunicar el impacto energético en una medida que el usuario entiende fácilmente: dinero.

---

## 20. Respuesta JSON

### Entrada

```json
{
  "consumo_kwh": 420,
  "uso_horario_pico": true,
  "cantidad_equipos": 10,
  "tipo_inmueble": "Casa",
  "horas_alto_consumo": 8
}
```

### Salida esperada

```json
{
  "categoria": "Moderado",
  "probabilidad": 0.82,
  "recomendaciones": [
    "Reducir el uso de equipos durante el horario pico",
    "Distribuir las actividades de mayor consumo"
  ],
  "costo_estimado_mensual": 315.00
}
```

> **Interpretación:** el resultado queda preparado para ser consumido directamente por el Back-End mediante una API REST.

---

## 21. Serialización

El modelo final se guarda para evitar volver a entrenarlo cada vez que la API recibe una solicitud.

### Artefactos

- `jouleai_modelo_random_forest_v6.joblib`
- `jouleai_model_metadata_v6_rf.json`
- `jouleai_benchmarks_recomendaciones_v6.json`
- `jouleai_leaderboard_8_modelos_v6.csv`

> **Interpretación:** el Back-End puede cargar el modelo serializado y utilizar exactamente el mismo preprocesamiento desarrollado en el notebook.

---

## 22. Integración con OCI

Los artefactos pueden almacenarse en **OCI Object Storage**.

La API puede desplegarse mediante:

- OCI Compute;
- OCI Functions;
- otros servicios OCI según la arquitectura elegida.

### Flujo sugerido

```text
Usuario
  ↓
API REST
  ↓
Validación del JSON
  ↓
Pipeline SVC v2.0.0 (One-Hot Encoding + StandardScaler)
  ↓
Categoría + Probabilidad
  ↓
Recomendaciones + Costo
  ↓
Respuesta JSON
```

---

## 23. Conclusión

JouleAI desarrolla un proceso completo de Ciencia de Datos y no solamente un clasificador.

El proyecto incluye:

- calidad de datos;
- EDA;
- nulos;
- outliers;
- análisis de leakage;
- creación de una etiqueta propia;
- comparación de 8 modelos;
- validación cruzada;
- selección multicriterio;
- calibración;
- evaluación final;
- interpretabilidad;
- recomendaciones;
- estimación financiera;
- serialización;
- preparación para API y OCI.

### Modelo final

**SVC v2.0.0 (SVM con kernel RBF), con escalado numérico dentro del pipeline.**

El artefacto de producción y `modelo_energia_v2.0.0_metadata.json` son la referencia para la carga, el orden de features y las métricas del servicio Flask.
