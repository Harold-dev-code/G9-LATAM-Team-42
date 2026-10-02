"""Feature selection for the trained model pipeline."""


def select_model_features(data: dict, feature_names: list[str]) -> dict:
    """Return the raw input fields in the order declared by model metadata."""
    missing = [name for name in feature_names if name not in data]
    if missing:
        raise ValueError(f"Missing model features: {', '.join(missing)}")
    return {name: data[name] for name in feature_names}
