"""
Flask application entry point.

Exposes:
- POST /predict: Receives user input, validates, computes derived features,
  runs the ML model, and returns the prediction.
- GET /health: Returns service health status.
"""

import hashlib
import json
import sys
import logging

import joblib
import pandas as pd
from flask import Flask, jsonify, request

from config import MODEL_METADATA_PATH, MODEL_PATH, PORT
from feature_engine import select_model_features
from validation import validate_prediction_request

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Load the model at startup
model = None
model_metadata = None


def load_model():
    """Load the model and its metadata, verifying the expected preprocessing."""
    global model, model_metadata
    try:
        with open(MODEL_METADATA_PATH, encoding="utf-8") as metadata_file:
            loaded_metadata = json.load(metadata_file)

        with open(MODEL_PATH, "rb") as model_file:
            model_hash = hashlib.file_digest(model_file, "sha256").hexdigest()
        if loaded_metadata.get("hash_modelo") != model_hash:
            raise ValueError("Model file hash does not match its metadata")
        if loaded_metadata.get("modelo_nombre") != "SVM":
            raise ValueError("The configured model must be the SVM v2 model")
        if loaded_metadata.get("tipo_modelo") != "SVC":
            raise ValueError("The configured model type must be SVC")
        if loaded_metadata.get("usa_escalado") is not True:
            raise ValueError("The configured SVC model must include feature scaling")
        if not isinstance(loaded_metadata.get("features"), list):
            raise ValueError("Model metadata must declare an ordered features list")

        model = joblib.load(MODEL_PATH)
        model_metadata = loaded_metadata
        logger.info(
            "Loaded %s model from %s",
            model_metadata["modelo_nombre"],
            MODEL_PATH,
        )
    except FileNotFoundError:
        logger.error("Model or metadata file not found")
        sys.exit(1)
    except Exception as e:
        logger.error("Failed to load model: %s", str(e))
        sys.exit(1)


@app.route("/predict", methods=["POST"])
def predict():
    """
    POST /predict
    Validates input, computes derived features, runs prediction, returns result.
    """
    data = request.get_json(force=True, silent=True)
    if data is None:
        return jsonify({"error": "Validation failed", "details": {"body": "Request body must be valid JSON"}}), 400

    # Validate input and apply defaults
    cleaned_data, errors = validate_prediction_request(data)
    if errors:
        return jsonify({"error": "Validation failed", "details": errors}), 400

    # The serialized pipeline handles encoding and scaling internally.
    feature_names = model_metadata["features"]
    features = select_model_features(cleaned_data, feature_names)

    # Create DataFrame with features in the correct order for the model
    feature_df = pd.DataFrame([features], columns=feature_names)

    # Run prediction
    try:
        categoria = model.predict(feature_df)[0]
        probabilities = model.predict_proba(feature_df)[0]
        # Get the probability of the predicted class
        class_index = list(model.classes_).index(categoria)
        probabilidad = float(probabilities[class_index])
    except Exception as e:
        logger.error("Prediction error: %s", str(e))
        return jsonify({"error": "Prediction failed", "details": str(e)}), 500

    return jsonify({
        "categoria": categoria,
        "probabilidad": round(probabilidad, 4)
    })


@app.route("/health", methods=["GET"])
def health():
    """
    GET /health
    Returns service health status.
    """
    return jsonify({
        "status": "ok",
        "model_loaded": model is not None
    })


# Load model when module is imported (app startup)
load_model()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=PORT, debug=False)
