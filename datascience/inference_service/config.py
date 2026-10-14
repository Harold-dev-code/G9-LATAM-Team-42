"""
Configuration for the Flask inference service.
"""
import os

# Path to the trained ML model pipeline
MODEL_PATH = os.path.join(os.path.dirname(__file__), 'modelo_energia_v2.0.0.pkl')
MODEL_METADATA_PATH = os.path.join(
	os.path.dirname(__file__), '..', 'modelo_energia_v2.0.0_metadata.json'
)

# Flask server port
PORT = 5000
