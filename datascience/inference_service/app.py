"""
Flask application entry point.

Exposes:
- POST /predict: Receives user input, validates, computes derived features,
  runs the ML model, and returns the prediction.
- GET /health: Returns service health status.
"""

import sys
import logging

import joblib
import pandas as pd
from flask import Flask, jsonify, request

from config import MODEL_PATH, PORT
from feature_engine import FEATURE_ORDER, compute_derived_features
from validation import validate_prediction_request

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Load the model at startup
model = None


def load_model():
    """Load the ML model pipeline from disk."""
    global model
    try:
        model = joblib.load(MODEL_PATH)
        logger.info("Model loaded successfully from %s", MODEL_PATH)
    except FileNotFoundError:
        logger.error("Model file not found at %s", MODEL_PATH)
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

    # Compute derived features (returns dict with all 15 features)
    features = compute_derived_features(cleaned_data)

    # Create DataFrame with features in the correct order for the model
    feature_df = pd.DataFrame([features], columns=FEATURE_ORDER)

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
