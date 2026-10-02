"""Tests for selecting raw model features in metadata order."""

import pytest

from feature_engine import select_model_features


class TestSelectModelFeatures:
    def test_selects_only_metadata_features_in_the_given_order(self):
        data = {
            "consumo_kwh": 350.0,
            "tipo_inmueble": "Casa",
            "personas_vivienda": 4,
            "cantidad_equipos": 8,
            "horas_alto_consumo": 5.0,
            "antiguedad_inmueble": 15,
            "uso_horario_pico": 1,
            "tiene_aire_acondicionado": 0,
            "tiene_calentador_electrico": 1,
            "electrodomesticos_eficientes": 0,
            "unrelated_field": "ignored",
        }
        feature_names = ["tipo_inmueble", "consumo_kwh", "personas_vivienda"]

        assert select_model_features(data, feature_names) == {
            "tipo_inmueble": "Casa",
            "consumo_kwh": 350.0,
            "personas_vivienda": 4,
        }

    def test_raises_for_a_feature_missing_from_validated_input(self):
        with pytest.raises(ValueError, match="unknown_feature"):
            select_model_features({"consumo_kwh": 350.0}, ["unknown_feature"])
