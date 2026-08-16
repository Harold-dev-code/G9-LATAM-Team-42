"""
Tests for the validation module.

Covers:
- Required field presence validation
- Range validation for numeric fields
- Enum validation for tipo_inmueble
- Binary field validation (0/1)
- Default value application for optional fields
"""

import pytest

from validation import validate_prediction_request


def _valid_request():
    """Returns a minimal valid request with only required fields."""
    return {
        "consumo_kwh": 350.0,
        "tipo_inmueble": "Casa",
        "personas_vivienda": 4,
        "cantidad_equipos": 8,
        "horas_alto_consumo": 5.0,
    }


def _full_valid_request():
    """Returns a complete valid request with all 10 fields."""
    return {
        "consumo_kwh": 350.0,
        "tipo_inmueble": "Casa",
        "personas_vivienda": 4,
        "cantidad_equipos": 8,
        "horas_alto_consumo": 5.0,
        "uso_horario_pico": 1,
        "antiguedad_inmueble": 15,
        "tiene_aire_acondicionado": 0,
        "tiene_calentador_electrico": 1,
        "electrodomesticos_eficientes": 0,
    }


class TestRequiredFields:
    """Tests for required field presence validation."""

    def test_all_required_fields_present(self):
        cleaned, errors = validate_prediction_request(_valid_request())
        assert errors == []
        assert "consumo_kwh" in cleaned
        assert "tipo_inmueble" in cleaned
        assert "personas_vivienda" in cleaned
        assert "cantidad_equipos" in cleaned
        assert "horas_alto_consumo" in cleaned

    def test_missing_consumo_kwh(self):
        data = _valid_request()
        del data["consumo_kwh"]
        _, errors = validate_prediction_request(data)
        assert any("consumo_kwh" in e for e in errors)

    def test_missing_tipo_inmueble(self):
        data = _valid_request()
        del data["tipo_inmueble"]
        _, errors = validate_prediction_request(data)
        assert any("tipo_inmueble" in e for e in errors)

    def test_missing_personas_vivienda(self):
        data = _valid_request()
        del data["personas_vivienda"]
        _, errors = validate_prediction_request(data)
        assert any("personas_vivienda" in e for e in errors)

    def test_missing_cantidad_equipos(self):
        data = _valid_request()
        del data["cantidad_equipos"]
        _, errors = validate_prediction_request(data)
        assert any("cantidad_equipos" in e for e in errors)

    def test_missing_horas_alto_consumo(self):
        data = _valid_request()
        del data["horas_alto_consumo"]
        _, errors = validate_prediction_request(data)
        assert any("horas_alto_consumo" in e for e in errors)

    def test_multiple_missing_fields(self):
        data = {"consumo_kwh": 350.0}
        _, errors = validate_prediction_request(data)
        assert len(errors) == 4  # 4 missing required fields

    def test_null_required_field(self):
        data = _valid_request()
        data["consumo_kwh"] = None
        _, errors = validate_prediction_request(data)
        assert any("consumo_kwh" in e for e in errors)


class TestRangeValidation:
    """Tests for numeric range validation."""

    def test_consumo_kwh_below_min(self):
        data = _valid_request()
        data["consumo_kwh"] = 49
        _, errors = validate_prediction_request(data)
        assert any("consumo_kwh" in e and "50" in e and "2000" in e for e in errors)

    def test_consumo_kwh_above_max(self):
        data = _valid_request()
        data["consumo_kwh"] = 2001
        _, errors = validate_prediction_request(data)
        assert any("consumo_kwh" in e for e in errors)

    def test_consumo_kwh_at_boundaries(self):
        data = _valid_request()
        data["consumo_kwh"] = 50
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["consumo_kwh"] == 50.0

        data["consumo_kwh"] = 2000
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["consumo_kwh"] == 2000.0

    def test_consumo_kwh_accepts_float(self):
        data = _valid_request()
        data["consumo_kwh"] = 150.5
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["consumo_kwh"] == 150.5

    def test_personas_vivienda_below_min(self):
        data = _valid_request()
        data["personas_vivienda"] = 0
        _, errors = validate_prediction_request(data)
        assert any("personas_vivienda" in e for e in errors)

    def test_personas_vivienda_above_max(self):
        data = _valid_request()
        data["personas_vivienda"] = 11
        _, errors = validate_prediction_request(data)
        assert any("personas_vivienda" in e for e in errors)

    def test_personas_vivienda_at_boundaries(self):
        data = _valid_request()
        data["personas_vivienda"] = 1
        _, errors = validate_prediction_request(data)
        assert errors == []

        data["personas_vivienda"] = 10
        _, errors = validate_prediction_request(data)
        assert errors == []

    def test_cantidad_equipos_below_min(self):
        data = _valid_request()
        data["cantidad_equipos"] = 0
        _, errors = validate_prediction_request(data)
        assert any("cantidad_equipos" in e for e in errors)

    def test_cantidad_equipos_above_max(self):
        data = _valid_request()
        data["cantidad_equipos"] = 21
        _, errors = validate_prediction_request(data)
        assert any("cantidad_equipos" in e for e in errors)

    def test_cantidad_equipos_at_boundaries(self):
        data = _valid_request()
        data["cantidad_equipos"] = 1
        _, errors = validate_prediction_request(data)
        assert errors == []

        data["cantidad_equipos"] = 20
        _, errors = validate_prediction_request(data)
        assert errors == []

    def test_horas_alto_consumo_below_min(self):
        data = _valid_request()
        data["horas_alto_consumo"] = -0.1
        _, errors = validate_prediction_request(data)
        assert any("horas_alto_consumo" in e for e in errors)

    def test_horas_alto_consumo_above_max(self):
        data = _valid_request()
        data["horas_alto_consumo"] = 24.1
        _, errors = validate_prediction_request(data)
        assert any("horas_alto_consumo" in e for e in errors)

    def test_horas_alto_consumo_at_boundaries(self):
        data = _valid_request()
        data["horas_alto_consumo"] = 0.0
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["horas_alto_consumo"] == 0.0

        data["horas_alto_consumo"] = 24.0
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["horas_alto_consumo"] == 24.0

    def test_horas_alto_consumo_accepts_int(self):
        data = _valid_request()
        data["horas_alto_consumo"] = 12
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["horas_alto_consumo"] == 12.0

    def test_antiguedad_inmueble_below_min(self):
        data = _valid_request()
        data["antiguedad_inmueble"] = 1
        _, errors = validate_prediction_request(data)
        assert any("antiguedad_inmueble" in e for e in errors)

    def test_antiguedad_inmueble_above_max(self):
        data = _valid_request()
        data["antiguedad_inmueble"] = 32
        _, errors = validate_prediction_request(data)
        assert any("antiguedad_inmueble" in e for e in errors)

    def test_antiguedad_inmueble_at_boundaries(self):
        data = _valid_request()
        data["antiguedad_inmueble"] = 2
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["antiguedad_inmueble"] == 2

        data["antiguedad_inmueble"] = 31
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["antiguedad_inmueble"] == 31


class TestEnumValidation:
    """Tests for tipo_inmueble enum validation."""

    @pytest.mark.parametrize(
        "tipo", ["Casa", "Oficina", "Apartamento", "Comercio"]
    )
    def test_valid_tipo_inmueble(self, tipo):
        data = _valid_request()
        data["tipo_inmueble"] = tipo
        _, errors = validate_prediction_request(data)
        assert errors == []

    @pytest.mark.parametrize(
        "tipo", ["casa", "OFICINA", "apartamento", "Hotel", "Bodega", ""]
    )
    def test_invalid_tipo_inmueble(self, tipo):
        data = _valid_request()
        data["tipo_inmueble"] = tipo
        _, errors = validate_prediction_request(data)
        assert any("tipo_inmueble" in e for e in errors)


class TestBinaryFieldValidation:
    """Tests for binary field (0/1) validation."""

    @pytest.mark.parametrize(
        "field",
        [
            "uso_horario_pico",
            "tiene_aire_acondicionado",
            "tiene_calentador_electrico",
            "electrodomesticos_eficientes",
        ],
    )
    def test_valid_binary_values(self, field):
        data = _valid_request()
        data[field] = 0
        _, errors = validate_prediction_request(data)
        assert errors == []

        data[field] = 1
        _, errors = validate_prediction_request(data)
        assert errors == []

    @pytest.mark.parametrize(
        "field",
        [
            "uso_horario_pico",
            "tiene_aire_acondicionado",
            "tiene_calentador_electrico",
            "electrodomesticos_eficientes",
        ],
    )
    def test_invalid_binary_values(self, field):
        data = _valid_request()
        data[field] = 2
        _, errors = validate_prediction_request(data)
        assert any(field in e for e in errors)

    @pytest.mark.parametrize(
        "field",
        [
            "uso_horario_pico",
            "tiene_aire_acondicionado",
            "tiene_calentador_electrico",
            "electrodomesticos_eficientes",
        ],
    )
    def test_negative_binary_values(self, field):
        data = _valid_request()
        data[field] = -1
        _, errors = validate_prediction_request(data)
        assert any(field in e for e in errors)


class TestDefaults:
    """Tests for default value application on optional fields."""

    def test_defaults_applied_when_optional_fields_absent(self):
        data = _valid_request()
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["uso_horario_pico"] == 0
        assert cleaned["antiguedad_inmueble"] == 10
        assert cleaned["tiene_aire_acondicionado"] == 0
        assert cleaned["tiene_calentador_electrico"] == 0
        assert cleaned["electrodomesticos_eficientes"] == 0

    def test_explicit_values_override_defaults(self):
        data = _full_valid_request()
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["uso_horario_pico"] == 1
        assert cleaned["antiguedad_inmueble"] == 15
        assert cleaned["tiene_aire_acondicionado"] == 0
        assert cleaned["tiene_calentador_electrico"] == 1
        assert cleaned["electrodomesticos_eficientes"] == 0

    def test_cleaned_data_has_all_10_fields(self):
        data = _valid_request()
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        expected_keys = {
            "consumo_kwh",
            "tipo_inmueble",
            "personas_vivienda",
            "cantidad_equipos",
            "horas_alto_consumo",
            "uso_horario_pico",
            "antiguedad_inmueble",
            "tiene_aire_acondicionado",
            "tiene_calentador_electrico",
            "electrodomesticos_eficientes",
        }
        assert set(cleaned.keys()) == expected_keys

    def test_partial_optional_fields(self):
        data = _valid_request()
        data["uso_horario_pico"] = 1
        data["antiguedad_inmueble"] = 20
        cleaned, errors = validate_prediction_request(data)
        assert errors == []
        assert cleaned["uso_horario_pico"] == 1
        assert cleaned["antiguedad_inmueble"] == 20
        assert cleaned["tiene_aire_acondicionado"] == 0
        assert cleaned["tiene_calentador_electrico"] == 0
        assert cleaned["electrodomesticos_eficientes"] == 0


# ============================================================================
# Property-Based Tests (Task 2.4)
# Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas
# Validates: Requirements 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7
# ============================================================================

from hypothesis import given, assume, settings
import hypothesis.strategies as st


class TestValidationProperty:
    """Property-based tests for validation module using Hypothesis."""

    @given(consumo_kwh=st.one_of(
        st.floats(max_value=49.9, allow_nan=False, allow_infinity=False),
        st.floats(min_value=2000.1, allow_nan=False, allow_infinity=False),
    ))
    @settings(max_examples=100)
    def test_invalid_consumo_kwh_rejected(self, consumo_kwh):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        data = {
            "consumo_kwh": consumo_kwh,
            "tipo_inmueble": "Casa",
            "personas_vivienda": 4,
            "cantidad_equipos": 8,
            "horas_alto_consumo": 5.0,
        }
        _, errors = validate_prediction_request(data)
        assert len(errors) > 0
        assert any("consumo_kwh" in e for e in errors)

    @given(personas=st.one_of(
        st.integers(max_value=0),
        st.integers(min_value=11),
    ))
    @settings(max_examples=100)
    def test_invalid_personas_vivienda_rejected(self, personas):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        data = {
            "consumo_kwh": 350.0,
            "tipo_inmueble": "Casa",
            "personas_vivienda": personas,
            "cantidad_equipos": 8,
            "horas_alto_consumo": 5.0,
        }
        _, errors = validate_prediction_request(data)
        assert len(errors) > 0
        assert any("personas_vivienda" in e for e in errors)

    @given(equipos=st.one_of(
        st.integers(max_value=0),
        st.integers(min_value=21),
    ))
    @settings(max_examples=100)
    def test_invalid_cantidad_equipos_rejected(self, equipos):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        data = {
            "consumo_kwh": 350.0,
            "tipo_inmueble": "Casa",
            "personas_vivienda": 4,
            "cantidad_equipos": equipos,
            "horas_alto_consumo": 5.0,
        }
        _, errors = validate_prediction_request(data)
        assert len(errors) > 0
        assert any("cantidad_equipos" in e for e in errors)

    @given(horas=st.one_of(
        st.floats(max_value=-0.01, allow_nan=False, allow_infinity=False),
        st.floats(min_value=24.01, allow_nan=False, allow_infinity=False),
    ))
    @settings(max_examples=100)
    def test_invalid_horas_alto_consumo_rejected(self, horas):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        data = {
            "consumo_kwh": 350.0,
            "tipo_inmueble": "Casa",
            "personas_vivienda": 4,
            "cantidad_equipos": 8,
            "horas_alto_consumo": horas,
        }
        _, errors = validate_prediction_request(data)
        assert len(errors) > 0
        assert any("horas_alto_consumo" in e for e in errors)

    @given(tipo=st.text(min_size=1, max_size=20).filter(
        lambda t: t not in {"Casa", "Oficina", "Apartamento", "Comercio"}
    ))
    @settings(max_examples=100)
    def test_invalid_tipo_inmueble_rejected(self, tipo):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        data = {
            "consumo_kwh": 350.0,
            "tipo_inmueble": tipo,
            "personas_vivienda": 4,
            "cantidad_equipos": 8,
            "horas_alto_consumo": 5.0,
        }
        _, errors = validate_prediction_request(data)
        assert len(errors) > 0
        assert any("tipo_inmueble" in e for e in errors)

    @given(value=st.integers().filter(lambda v: v not in (0, 1)))
    @settings(max_examples=100)
    def test_invalid_binary_field_rejected(self, value):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        # Test each binary field with invalid values
        binary_fields = [
            "uso_horario_pico",
            "tiene_aire_acondicionado",
            "tiene_calentador_electrico",
            "electrodomesticos_eficientes",
        ]
        for field in binary_fields:
            data = {
                "consumo_kwh": 350.0,
                "tipo_inmueble": "Casa",
                "personas_vivienda": 4,
                "cantidad_equipos": 8,
                "horas_alto_consumo": 5.0,
                field: value,
            }
            _, errors = validate_prediction_request(data)
            assert len(errors) > 0, f"Expected error for {field}={value}"

    @given(antiguedad=st.one_of(
        st.integers(max_value=1),
        st.integers(min_value=32),
    ))
    @settings(max_examples=100)
    def test_invalid_antiguedad_inmueble_rejected(self, antiguedad):
        """Feature: joule-ia-ml-integration, Property 3: Validación Flask rechaza entradas inválidas"""
        data = {
            "consumo_kwh": 350.0,
            "tipo_inmueble": "Casa",
            "personas_vivienda": 4,
            "cantidad_equipos": 8,
            "horas_alto_consumo": 5.0,
            "antiguedad_inmueble": antiguedad,
        }
        _, errors = validate_prediction_request(data)
        assert len(errors) > 0
        assert any("antiguedad_inmueble" in e for e in errors)
