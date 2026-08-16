"""
Feature engineering module.

Computes derived features required by the ML model:
- consumo_promedio_diario = consumo_kwh / 30
- ratio_persona_kwh = consumo_kwh / personas_vivienda
- consumo_por_equipo = consumo_kwh / cantidad_equipos
- consumo_por_hora_pico = consumo_kwh / horas_alto_consumo (0 if horas_alto_consumo == 0)
- costo_estimado_mensual = consumo_kwh * 0.75

Returns a dictionary with all 15 features in the order expected by the model pipeline.
"""


# Ordered list of feature names as expected by the model pipeline (from model.feature_names_in_)
FEATURE_ORDER = [
    "tipo_inmueble",
    "personas_vivienda",
    "antiguedad_inmueble",
    "cantidad_equipos",
    "tiene_aire_acondicionado",
    "tiene_calentador_electrico",
    "electrodomesticos_eficientes",
    "uso_horario_pico",
    "horas_alto_consumo",
    "consumo_kwh",
    "consumo_promedio_diario",
    "ratio_persona_kwh",
    "consumo_por_equipo",
    "consumo_por_hora_pico",
    "costo_estimado_mensual",
]


def compute_derived_features(data: dict) -> dict:
    """
    Computes the 5 derived features and returns a dictionary with all 15 features
    in the order expected by the model pipeline.

    Args:
        data: Validated dictionary with the 10 user-provided fields
              (defaults already applied for optional fields).

    Returns:
        Dictionary with all 15 features ordered for the model pipeline.

    Features computed:
        - consumo_promedio_diario = consumo_kwh / 30
        - ratio_persona_kwh = consumo_kwh / personas_vivienda
        - consumo_por_equipo = consumo_kwh / cantidad_equipos
        - consumo_por_hora_pico = consumo_kwh / horas_alto_consumo (0 if horas == 0)
        - costo_estimado_mensual = consumo_kwh * 0.75
    """
    consumo_kwh = data["consumo_kwh"]
    personas_vivienda = data["personas_vivienda"]
    cantidad_equipos = data["cantidad_equipos"]
    horas_alto_consumo = data["horas_alto_consumo"]

    consumo_promedio_diario = consumo_kwh / 30
    ratio_persona_kwh = consumo_kwh / personas_vivienda
    consumo_por_equipo = consumo_kwh / cantidad_equipos
    consumo_por_hora_pico = (
        consumo_kwh / horas_alto_consumo if horas_alto_consumo >= 0.01 else 0.0
    )
    costo_estimado_mensual = consumo_kwh * 0.75

    features = {
        "tipo_inmueble": data["tipo_inmueble"],
        "personas_vivienda": personas_vivienda,
        "antiguedad_inmueble": data["antiguedad_inmueble"],
        "cantidad_equipos": cantidad_equipos,
        "tiene_aire_acondicionado": data["tiene_aire_acondicionado"],
        "tiene_calentador_electrico": data["tiene_calentador_electrico"],
        "electrodomesticos_eficientes": data["electrodomesticos_eficientes"],
        "uso_horario_pico": data["uso_horario_pico"],
        "horas_alto_consumo": horas_alto_consumo,
        "consumo_kwh": consumo_kwh,
        "consumo_promedio_diario": consumo_promedio_diario,
        "ratio_persona_kwh": ratio_persona_kwh,
        "consumo_por_equipo": consumo_por_equipo,
        "consumo_por_hora_pico": consumo_por_hora_pico,
        "costo_estimado_mensual": costo_estimado_mensual,
    }

    return features
