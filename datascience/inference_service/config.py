"""
Configuration for the Flask inference service.
"""
import os

# Path to the trained ML model pipeline
MODEL_PATH = os.path.join(os.path.dirname(__file__), 'modelo_arbol_decision_pipeline.pkl')

# Flask server port
PORT = 5000
