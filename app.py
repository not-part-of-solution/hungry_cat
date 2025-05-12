from flask import Flask, request, jsonify
import cv2
import numpy as np
import tensorflow as tf
import os
import gdown

app = Flask(__name__)

# Путь к вашей модели .h5
MODEL_URL = 'https://drive.google.com/file/d/1ha9UT3lJgkJLwv1hOAp0HwBd76nXmapT/view?usp=drive_link'
MODEL_PATH = 'cat_model.h5'

# Параметры
IMAGE_SIZE = (224, 224)
UNKNOWN_THRESHOLD = 0.6

# Метки классов (замените на свои)
CLASS_LABELS = {
    0: 'unknown',
    1: 'Авель',
    2: 'Бисмарк',
    3: 'Буся',
    4: 'Винстон',
    5: 'Ева',
    6: 'Изумруд',
    7: 'Муся'
}

# Загрузка модели один раз при старте сервиса
model = tf.keras.models.load_model(MODEL_PATH)

def process_image(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, IMAGE_SIZE)
    img = img / 255.0
    return np.expand_dims(img, axis=0)

@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files:
        return jsonify({'error': 'No image'}), 400

    file = request.files['image']
    temp_path = 'temp.jpg'
    file.save(temp_path)

    processed_img = process_image(temp_path)
    os.remove(temp_path)

    if processed_img is None:
        return jsonify({'error': 'Invalid image'}), 400

    predictions = model.predict(processed_img)[0]
    class_id = int(np.argmax(predictions))
    confidence = float(predictions[class_id])

    label = CLASS_LABELS.get(class_id, 'unknown')
    return jsonify({'class': label, 'confidence': confidence})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)