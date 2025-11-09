from flask import Flask, request, jsonify
from transformers import pipeline
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
app = Flask(__name__)

logger.info("Loading sentiment model...")
sentiment_pipeline = pipeline(
    "sentiment-analysis",
    model="distilbert-base-uncased-finetuned-sst-2-english"
)

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "healthy"}), 200

@app.route("/ready", methods=["GET"])
def ready():
    try:
        sentiment_pipeline("test")
        return jsonify({"status": "ready"}), 200
    except Exception as e:
        return jsonify({"status": "not ready", "error": str(e)}), 503

@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.json
        if not data or 'text' not in data:
            return jsonify({"error": "Missing 'text' field"}), 400

        text = data['text']
        if not text or len(text.strip()) == 0:
            return jsonify({"error": "Text can not be empty"}), 400

        result = sentiment_pipeline(text[:512])[0]
        return jsonify({
            "text": text,
            "sentiment": result['label'],
            "confidence": round(result['score'], 4)
        }), 200
    except Exception as e:
        logger.error(f"Error during prediction: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)