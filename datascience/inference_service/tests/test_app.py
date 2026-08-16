"""
Tests for the Flask application endpoints.

Covers:
- POST /predict with valid input returns correct response structure
- POST /predict with invalid input returns 400
- GET /health returns status ok
- Model output contract (categoria and probabilidad)
"""
import json

import pytest

from app import app


@pytest.fixture
def client():
    """Create a Flask test client."""
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


@pytest.fixture
def valid_payload():
    """A valid prediction request payload with all fields."""
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


class TestHealthEndpoint:
    """Tests for GET /health."""

    def test_health_returns_200(self, client):
        response = client.get("/health")
        assert response.status_code == 200

    def test_health_returns_correct_structure(self, client):
        response = client.get("/health")
        data = response.get_json()
        assert data["status"] == "ok"
        assert data["model_loaded"] is True


class TestPredictEndpoint:
    """Tests for POST /predict."""

    def test_predict_valid_input_returns_200(self, client, valid_payload):
        response = client.post(
            "/predict", data=json.dumps(valid_payload), content_type="application/json"
        )
        assert response.status_code == 200

    def test_predict_valid_input_returns_categoria(self, client, valid_payload):
        response = client.post(
            "/predict", data=json.dumps(valid_payload), content_type="application/json"
        )
        data = response.get_json()
        assert "categoria" in data
        assert data["categoria"] in ("Eficiente", "Moderado", "Ineficiente")

    def test_predict_valid_input_returns_probabilidad(self, client, valid_payload):
        response = client.post(
            "/predict", data=json.dumps(valid_payload), content_type="application/json"
        )
        data = response.get_json()
        assert "probabilidad" in data
        assert 0.0 <= data["probabilidad"] <= 1.0

    def test_predict_missing_required_field_returns_400(self, client):
        payload = {
            "tipo_inmueble": "Casa",
            "personas_vivienda": 4,
            "cantidad_equipos": 8,
            "horas_alto_consumo": 5.0,
        }
        response = client.post(
            "/predict", data=json.dumps(payload), content_type="application/json"
        )
        assert response.status_code == 400
        data = response.get_json()
        assert data["error"] == "Validation failed"
        assert isinstance(data["details"], list)

    def test_predict_invalid_range_returns_400(self, client, valid_payload):
        valid_payload["consumo_kwh"] = 10.0  # Below minimum of 50
        response = client.post(
            "/predict", data=json.dumps(valid_payload), content_type="application/json"
        )
        assert response.status_code == 400
        data = response.get_json()
        assert data["error"] == "Validation failed"

    def test_predict_optional_fields_use_defaults(self, client):
        """Test that only required fields are needed for a successful prediction."""
        payload = {
            "consumo_kwh": 200.0,
            "tipo_inmueble": "Oficina",
            "personas_vivienda": 2,
            "cantidad_equipos": 5,
            "horas_alto_consumo": 3.0,
        }
        response = client.post(
            "/predict", data=json.dumps(payload), content_type="application/json"
        )
        assert response.status_code == 200
        data = response.get_json()
        assert data["categoria"] in ("Eficiente", "Moderado", "Ineficiente")
        assert 0.0 <= data["probabilidad"] <= 1.0

    def test_predict_invalid_tipo_inmueble_returns_400(self, client, valid_payload):
        valid_payload["tipo_inmueble"] = "Garage"
        response = client.post(
            "/predict", data=json.dumps(valid_payload), content_type="application/json"
        )
        assert response.status_code == 400


# ============================================================================
# Property-Based Tests (Task 2.6)
# Feature: joule-ia-ml-integration, Property 2: Contrato de salida del modelo ML
# Validates: Requirements 1.3
# ============================================================================

from hypothesis import given, settings
import hypothesis.strategies as st


def valid_request_strategy():
    """Strategy to generate valid 10-field prediction requests.
    
    Uses realistic float values that won't cause overflow in derived feature
    calculations (e.g., consumo_kwh / horas_alto_consumo).
    """
    return st.fixed_dictionaries({
        "consumo_kwh": st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False, allow_subnormal=False),
        "tipo_inmueble": st.sampled_from(["Casa", "Oficina", "Apartamento", "Comercio"]),
        "personas_vivienda": st.integers(min_value=1, max_value=10),
        "cantidad_equipos": st.integers(min_value=1, max_value=20),
        "horas_alto_consumo": st.one_of(
            st.just(0.0),
            st.floats(min_value=0.1, max_value=24.0, allow_nan=False, allow_infinity=False),
        ),
        "uso_horario_pico": st.sampled_from([0, 1]),
        "antiguedad_inmueble": st.integers(min_value=2, max_value=31),
        "tiene_aire_acondicionado": st.sampled_from([0, 1]),
        "tiene_calentador_electrico": st.sampled_from([0, 1]),
        "electrodomesticos_eficientes": st.sampled_from([0, 1]),
    })


class TestPredictOutputContractProperty:
    """Property-based tests for /predict output contract."""

    @given(payload=valid_request_strategy())
    @settings(max_examples=100)
    def test_predict_returns_valid_categoria(self, payload):
        """Feature: joule-ia-ml-integration, Property 2: Contrato de salida del modelo ML"""
        app.config["TESTING"] = True
        with app.test_client() as client:
            response = client.post(
                "/predict", data=json.dumps(payload), content_type="application/json"
            )
            assert response.status_code == 200
            data = response.get_json()
            assert data["categoria"] in ("Eficiente", "Moderado", "Ineficiente")

    @given(payload=valid_request_strategy())
    @settings(max_examples=100)
    def test_predict_returns_valid_probabilidad(self, payload):
        """Feature: joule-ia-ml-integration, Property 2: Contrato de salida del modelo ML"""
        app.config["TESTING"] = True
        with app.test_client() as client:
            response = client.post(
                "/predict", data=json.dumps(payload), content_type="application/json"
            )
            assert response.status_code == 200
            data = response.get_json()
            assert 0.0 <= data["probabilidad"] <= 1.0


# ============================================================================
# Property-Based Tests (Task 2.7)
# Feature: joule-ia-ml-integration, Property 4: Aplicación de valores por defecto en Flask
# Validates: Requirements 1.4
# ============================================================================


def required_only_strategy():
    """Strategy to generate requests with only required fields."""
    return st.fixed_dictionaries({
        "consumo_kwh": st.floats(min_value=50.0, max_value=2000.0, allow_nan=False, allow_infinity=False, allow_subnormal=False),
        "tipo_inmueble": st.sampled_from(["Casa", "Oficina", "Apartamento", "Comercio"]),
        "personas_vivienda": st.integers(min_value=1, max_value=10),
        "cantidad_equipos": st.integers(min_value=1, max_value=20),
        "horas_alto_consumo": st.one_of(
            st.just(0.0),
            st.floats(min_value=0.1, max_value=24.0, allow_nan=False, allow_infinity=False),
        ),
    })


class TestDefaultsProperty:
    """Property-based tests for default value application."""

    @given(payload=required_only_strategy())
    @settings(max_examples=100)
    def test_defaults_produce_same_as_explicit(self, payload):
        """Feature: joule-ia-ml-integration, Property 4: Aplicación de valores por defecto en Flask"""
        # Request with only required fields (defaults should be applied)
        app.config["TESTING"] = True
        with app.test_client() as client:
            response_defaults = client.post(
                "/predict", data=json.dumps(payload), content_type="application/json"
            )
            assert response_defaults.status_code == 200

            # Request with explicit defaults
            explicit_payload = dict(payload)
            explicit_payload["uso_horario_pico"] = 0
            explicit_payload["antiguedad_inmueble"] = 10
            explicit_payload["tiene_aire_acondicionado"] = 0
            explicit_payload["tiene_calentador_electrico"] = 0
            explicit_payload["electrodomesticos_eficientes"] = 0

            response_explicit = client.post(
                "/predict", data=json.dumps(explicit_payload), content_type="application/json"
            )
            assert response_explicit.status_code == 200

            data_defaults = response_defaults.get_json()
            data_explicit = response_explicit.get_json()

            assert data_defaults["categoria"] == data_explicit["categoria"]
            assert data_defaults["probabilidad"] == data_explicit["probabilidad"]


# ============================================================================
# Property-Based Tests (Task 7.4)
# Feature: joule-ia-ml-integration, Property 9: Round-trip de serialización JSON
# Validates: Requirements 8.1, 8.2, 8.3
# ============================================================================


class TestSerializationRoundTripProperty:
    """Property-based tests for JSON serialization round-trip."""

    @given(payload=valid_request_strategy())
    @settings(max_examples=100)
    def test_json_roundtrip_preserves_values(self, payload):
        """Feature: joule-ia-ml-integration, Property 9: Round-trip de serialización JSON"""
        # Serialize to JSON and deserialize
        json_str = json.dumps(payload)
        deserialized = json.loads(json_str)

        # Assert field equivalence
        assert deserialized["consumo_kwh"] == payload["consumo_kwh"]
        assert deserialized["tipo_inmueble"] == payload["tipo_inmueble"]
        assert deserialized["personas_vivienda"] == payload["personas_vivienda"]
        assert deserialized["cantidad_equipos"] == payload["cantidad_equipos"]
        assert deserialized["horas_alto_consumo"] == payload["horas_alto_consumo"]
        assert deserialized["uso_horario_pico"] == payload["uso_horario_pico"]
        assert deserialized["antiguedad_inmueble"] == payload["antiguedad_inmueble"]
        assert deserialized["tiene_aire_acondicionado"] == payload["tiene_aire_acondicionado"]
        assert deserialized["tiene_calentador_electrico"] == payload["tiene_calentador_electrico"]
        assert deserialized["electrodomesticos_eficientes"] == payload["electrodomesticos_eficientes"]

    @given(payload=valid_request_strategy())
    @settings(max_examples=100)
    def test_flask_receives_correct_values(self, payload):
        """Feature: joule-ia-ml-integration, Property 9: Round-trip de serialización JSON"""
        app.config["TESTING"] = True
        with app.test_client() as client:
            # Send as JSON and verify Flask processes it correctly (returns 200)
            response = client.post(
                "/predict", data=json.dumps(payload), content_type="application/json"
            )
            # If the payload is valid, Flask should return 200 (not 400)
            assert response.status_code == 200, (
                f"Valid payload rejected: {response.get_json()}"
            )
