"""
Tests for the feature engine module.

Covers:
- Correct calculation of consumo_promedio_diario
- Correct calculation of ratio_persona_kwh
- Correct calculation of consumo_por_equipo
- Correct calculation of consumo_por_hora_pico
- Correct calculation of costo_estimado_mensual
- Output dictionary contains all 15 features
- Feature order matches model pipeline expectations
"""

import pytest

from feature_engine import compute_derived_features, FEATURE_ORDER


def _make_valid_input(**overrides):
    """Helper to create a valid 10-field input dict with optional overrides."""
    base = {
        "consumo_kwh": 300.0,
        "uso_horario_pico": 1,
        "cantidad_equipos": 5,
        "tipo_inmueble": "Casa",
        "horas_alto_consumo": 6.0,
        "personas_vivienda": 4,
        "antiguedad_inmueble": 10,
        "tiene_aire_acondicionado": 0,
        "tiene_calentador_electrico": 1,
        "electrodomesticos_eficientes": 0,
    }
    base.update(overrides)
    return base


class TestComputeDerivedFeatures:
    """Unit tests for compute_derived_features."""

    def test_calculates_consumo_promedio_diario(self):
        data = _make_valid_input(consumo_kwh=600.0)
        result = compute_derived_features(data)
        assert result["consumo_promedio_diario"] == 600.0 / 30

    def test_calculates_ratio_persona_kwh(self):
        data = _make_valid_input(consumo_kwh=400.0, personas_vivienda=2)
        result = compute_derived_features(data)
        assert result["ratio_persona_kwh"] == 400.0 / 2

    def test_calculates_consumo_por_equipo(self):
        data = _make_valid_input(consumo_kwh=500.0, cantidad_equipos=10)
        result = compute_derived_features(data)
        assert result["consumo_por_equipo"] == 500.0 / 10

    def test_calculates_consumo_por_hora_pico(self):
        data = _make_valid_input(consumo_kwh=480.0, horas_alto_consumo=8.0)
        result = compute_derived_features(data)
        assert result["consumo_por_hora_pico"] == 480.0 / 8.0

    def test_consumo_por_hora_pico_zero_hours(self):
        data = _make_valid_input(consumo_kwh=300.0, horas_alto_consumo=0.0)
        result = compute_derived_features(data)
        assert result["consumo_por_hora_pico"] == 0.0

    def test_consumo_por_hora_pico_near_zero_hours(self):
        data = _make_valid_input(consumo_kwh=300.0, horas_alto_consumo=0.005)
        result = compute_derived_features(data)
        assert result["consumo_por_hora_pico"] == 0.0

    def test_calculates_costo_estimado_mensual(self):
        data = _make_valid_input(consumo_kwh=400.0)
        result = compute_derived_features(data)
        assert result["costo_estimado_mensual"] == 400.0 * 0.75

    def test_returns_all_15_features(self):
        data = _make_valid_input()
        result = compute_derived_features(data)
        assert len(result) == 15

    def test_contains_all_expected_keys(self):
        data = _make_valid_input()
        result = compute_derived_features(data)
        for key in FEATURE_ORDER:
            assert key in result, f"Missing key: {key}"

    def test_preserves_original_fields(self):
        data = _make_valid_input(
            consumo_kwh=150.0,
            uso_horario_pico=0,
            cantidad_equipos=3,
            tipo_inmueble="Oficina",
            horas_alto_consumo=2.5,
            personas_vivienda=2,
            antiguedad_inmueble=15,
            tiene_aire_acondicionado=1,
            tiene_calentador_electrico=0,
            electrodomesticos_eficientes=1,
        )
        result = compute_derived_features(data)
        assert result["consumo_kwh"] == 150.0
        assert result["uso_horario_pico"] == 0
        assert result["cantidad_equipos"] == 3
        assert result["tipo_inmueble"] == "Oficina"
        assert result["horas_alto_consumo"] == 2.5
        assert result["personas_vivienda"] == 2
        assert result["antiguedad_inmueble"] == 15
        assert result["tiene_aire_acondicionado"] == 1
        assert result["tiene_calentador_electrico"] == 0
        assert result["electrodomesticos_eficientes"] == 1

    def test_feature_order_matches_constant(self):
        data = _make_valid_input()
        result = compute_derived_features(data)
        assert list(result.keys()) == FEATURE_ORDER

    def test_boundary_min_values(self):
        data = _make_valid_input(consumo_kwh=50.0, personas_vivienda=1, cantidad_equipos=1)
        result = compute_derived_features(data)
        assert result["consumo_promedio_diario"] == 50.0 / 30
        assert result["ratio_persona_kwh"] == 50.0 / 1
        assert result["consumo_por_equipo"] == 50.0 / 1
        assert result["costo_estimado_mensual"] == 50.0 * 0.75

    def test_boundary_max_values(self):
        data = _make_valid_input(consumo_kwh=2000.0, personas_vivienda=10, cantidad_equipos=20)
        result = compute_derived_features(data)
        assert result["consumo_promedio_diario"] == 2000.0 / 30
        assert result["ratio_persona_kwh"] == 2000.0 / 10
        assert result["consumo_por_equipo"] == 2000.0 / 20
        assert result["costo_estimado_mensual"] == 2000.0 * 0.75


# ============================================================================
# Property-Based Tests (Task 2.5)
# Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados
# Validates: Requirements 1.2
# ============================================================================

from hypothesis import given, settings
import hypothesis.strategies as st


class TestFeatureEngineProperty:
    """Property-based tests for feature engine using Hypothesis."""

    @given(
        consumo=st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False),
        personas=st.integers(min_value=1, max_value=10),
    )
    @settings(max_examples=200)
    def test_consumo_promedio_diario_property(self, consumo, personas):
        """Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados"""
        data = _make_valid_input(consumo_kwh=consumo, personas_vivienda=personas)
        result = compute_derived_features(data)
        assert result["consumo_promedio_diario"] == consumo / 30

    @given(
        consumo=st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False),
        personas=st.integers(min_value=1, max_value=10),
    )
    @settings(max_examples=200)
    def test_ratio_persona_kwh_property(self, consumo, personas):
        """Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados"""
        data = _make_valid_input(consumo_kwh=consumo, personas_vivienda=personas)
        result = compute_derived_features(data)
        assert result["ratio_persona_kwh"] == consumo / personas

    @given(
        consumo=st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False),
        equipos=st.integers(min_value=1, max_value=20),
    )
    @settings(max_examples=200)
    def test_consumo_por_equipo_property(self, consumo, equipos):
        """Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados"""
        data = _make_valid_input(consumo_kwh=consumo, cantidad_equipos=equipos)
        result = compute_derived_features(data)
        assert result["consumo_por_equipo"] == consumo / equipos

    @given(
        consumo=st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False),
        horas=st.floats(min_value=0.01, max_value=24.0, allow_nan=False, allow_infinity=False),
    )
    @settings(max_examples=200)
    def test_consumo_por_hora_pico_property(self, consumo, horas):
        """Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados"""
        data = _make_valid_input(consumo_kwh=consumo, horas_alto_consumo=horas)
        result = compute_derived_features(data)
        assert result["consumo_por_hora_pico"] == consumo / horas

    @given(
        consumo=st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False),
    )
    @settings(max_examples=200)
    def test_costo_estimado_mensual_property(self, consumo):
        """Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados"""
        data = _make_valid_input(consumo_kwh=consumo)
        result = compute_derived_features(data)
        assert result["costo_estimado_mensual"] == consumo * 0.75

    @given(
        consumo=st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False),
        personas=st.integers(min_value=1, max_value=10),
        equipos=st.integers(min_value=1, max_value=20),
        horas=st.floats(min_value=0.0, max_value=24.0, allow_nan=False, allow_infinity=False),
    )
    @settings(max_examples=200)
    def test_output_has_15_features_property(self, consumo, personas, equipos, horas):
        """Feature: joule-ia-ml-integration, Property 1: Cálculo correcto de features derivados"""
        data = _make_valid_input(
            consumo_kwh=consumo,
            personas_vivienda=personas,
            cantidad_equipos=equipos,
            horas_alto_consumo=horas,
        )
        result = compute_derived_features(data)
        assert len(result) == 15
        assert list(result.keys()) == FEATURE_ORDER
