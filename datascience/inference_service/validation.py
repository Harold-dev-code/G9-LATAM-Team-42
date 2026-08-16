"""
Input validation module for prediction requests.

Validates required fields, ranges, enum values, and binary constraints.
Applies default values for optional fields when absent.
"""

# Required fields that must be present in the request
REQUIRED_FIELDS = [
    "consumo_kwh",
    "tipo_inmueble",
    "personas_vivienda",
    "cantidad_equipos",
    "horas_alto_consumo",
]

# Valid values for tipo_inmueble
VALID_TIPO_INMUEBLE = {"Casa", "Oficina", "Apartamento", "Comercio"}

# Binary fields that must be 0 or 1
BINARY_FIELDS = [
    "uso_horario_pico",
    "tiene_aire_acondicionado",
    "tiene_calentador_electrico",
    "electrodomesticos_eficientes",
]

# Default values for optional fields
DEFAULTS = {
    "uso_horario_pico": 0,
    "antiguedad_inmueble": 10,
    "tiene_aire_acondicionado": 0,
    "tiene_calentador_electrico": 0,
    "electrodomesticos_eficientes": 0,
}


def validate_prediction_request(data: dict) -> tuple[dict, list[str]]:
    """
    Validate the prediction request fields and apply defaults for optional fields.

    Args:
        data: Dictionary containing the request fields.

    Returns:
        A tuple of (cleaned_data, errors).
        - cleaned_data: dict with all 10 fields, defaults applied for missing optionals.
        - errors: list of error strings. If non-empty, validation failed.
    """
    errors: list[str] = []
    cleaned: dict = {}

    # 1. Check required fields presence
    for field in REQUIRED_FIELDS:
        if field not in data or data[field] is None:
            errors.append(f"El campo '{field}' es obligatorio")

    # If required fields are missing, return early (can't validate ranges)
    if errors:
        # Still apply defaults for optional fields in cleaned data
        for key, default in DEFAULTS.items():
            cleaned[key] = data.get(key, default)
        # Copy whatever required fields exist
        for field in REQUIRED_FIELDS:
            if field in data and data[field] is not None:
                cleaned[field] = data[field]
        return cleaned, errors

    # 2. Validate consumo_kwh: numeric, range 50-2000
    consumo_kwh = data["consumo_kwh"]
    if not isinstance(consumo_kwh, (int, float)):
        errors.append("El campo 'consumo_kwh' debe ser numérico")
    elif consumo_kwh < 50 or consumo_kwh > 2000:
        errors.append("El valor de 'consumo_kwh' debe estar entre 50 y 2000")
    else:
        cleaned["consumo_kwh"] = float(consumo_kwh)

    # 3. Validate tipo_inmueble: must be one of the valid values
    tipo_inmueble = data["tipo_inmueble"]
    if tipo_inmueble not in VALID_TIPO_INMUEBLE:
        errors.append(
            f"El valor de 'tipo_inmueble' debe ser uno de: "
            f"{', '.join(sorted(VALID_TIPO_INMUEBLE))}"
        )
    else:
        cleaned["tipo_inmueble"] = tipo_inmueble

    # 4. Validate personas_vivienda: integer, range 1-10
    personas_vivienda = data["personas_vivienda"]
    if not isinstance(personas_vivienda, int) or isinstance(personas_vivienda, bool):
        errors.append("El campo 'personas_vivienda' debe ser un entero")
    elif personas_vivienda < 1 or personas_vivienda > 10:
        errors.append("El valor de 'personas_vivienda' debe estar entre 1 y 10")
    else:
        cleaned["personas_vivienda"] = personas_vivienda

    # 5. Validate cantidad_equipos: integer, range 1-20
    cantidad_equipos = data["cantidad_equipos"]
    if not isinstance(cantidad_equipos, int) or isinstance(cantidad_equipos, bool):
        errors.append("El campo 'cantidad_equipos' debe ser un entero")
    elif cantidad_equipos < 1 or cantidad_equipos > 20:
        errors.append("El valor de 'cantidad_equipos' debe estar entre 1 y 20")
    else:
        cleaned["cantidad_equipos"] = cantidad_equipos

    # 6. Validate horas_alto_consumo: numeric, range 0.0-24.0
    horas_alto_consumo = data["horas_alto_consumo"]
    if not isinstance(horas_alto_consumo, (int, float)):
        errors.append("El campo 'horas_alto_consumo' debe ser numérico")
    elif horas_alto_consumo < 0.0 or horas_alto_consumo > 24.0:
        errors.append("El valor de 'horas_alto_consumo' debe estar entre 0.0 y 24.0")
    else:
        cleaned["horas_alto_consumo"] = float(horas_alto_consumo)

    # 7. Apply defaults and validate optional fields
    # antiguedad_inmueble: integer, range 2-31 (only validated if provided)
    antiguedad_inmueble = data.get("antiguedad_inmueble")
    if antiguedad_inmueble is None:
        cleaned["antiguedad_inmueble"] = DEFAULTS["antiguedad_inmueble"]
    elif not isinstance(antiguedad_inmueble, int) or isinstance(antiguedad_inmueble, bool):
        errors.append("El campo 'antiguedad_inmueble' debe ser un entero")
    elif antiguedad_inmueble < 2 or antiguedad_inmueble > 31:
        errors.append("El valor de 'antiguedad_inmueble' debe estar entre 2 y 31")
    else:
        cleaned["antiguedad_inmueble"] = antiguedad_inmueble

    # Binary fields: validate 0 or 1, apply default if absent
    for field in BINARY_FIELDS:
        value = data.get(field)
        if value is None:
            cleaned[field] = DEFAULTS[field]
        elif not isinstance(value, int) or isinstance(value, bool):
            errors.append(f"El campo '{field}' debe ser un entero (0 o 1)")
        elif value not in (0, 1):
            errors.append(f"El valor de '{field}' debe ser 0 o 1")
        else:
            cleaned[field] = value

    return cleaned, errors
