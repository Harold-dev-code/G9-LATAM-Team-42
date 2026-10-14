FROM python:3.12-slim

WORKDIR /app

COPY inference_service/requirements.txt ./requirements.txt

RUN pip install --no-cache-dir -r requirements.txt

COPY inference_service/app.py inference_service/config.py inference_service/feature_engine.py inference_service/validation.py ./inference_service/
COPY inference_service/modelo_energia_v2.0.0.pkl ./inference_service/
COPY modelo_energia_v2.0.0_metadata.json ./

WORKDIR /app/inference_service

EXPOSE 5000

CMD ["python", "app.py"]