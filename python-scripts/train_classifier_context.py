import numpy as np
import tensorflow as tf

print("🚀 Starting training for context classifier...")

# ------------------------
# 1️⃣ Dataset (Improved)
# ------------------------
texts = [
    # COMMAND
    "add milk to my list",
    "create a new list",
    "delete my grocery list",
    "set a reminder at 5 pm",
    "remind me tomorrow",
    "add john to contacts",
    "delete contact named mike",
    "turn off the lights",
    "play music",
    "create a new task",
    "remove bread from my list",
    "set alarm for morning",

    # ETIQUETTE
    "hi", "hello", "hey", "good morning", "good evening",
    "thank you", "thanks a lot", "how are you", "greetings",

    # QUERY (improved & expanded)
    "what is my username",
    "show my email",
    "what is my phone number",
    "tell me my address",
    "what is the AI's name",
    "show AI's email",
    "what is the AI's contact number",
    "tell me the AI's address",
    "tell me about cats",
    "what do you know about dogs",
    "give me information on quantum physics",
    "what do you know about Python programming",

    # FORECAST
    "it will rain tomorrow",
    "stock prices will rise",
    "the weather will be hot",
    "traffic will be heavy",
    "temperature will drop tonight",
]

labels_text = (
    ["command"] * 12 +
    ["etiquette"] * 9 +
    ["query"] * 12 +  # increased queries
    ["forecast"] * 5
)

# ------------------------
# 2️⃣ Encode labels
# ------------------------
label_map = {"command": 0, "etiquette": 1, "query": 2, "forecast": 3}
labels = np.array([label_map[l] for l in labels_text])
labels = tf.keras.utils.to_categorical(labels, num_classes=4)

# ------------------------
# 3️⃣ Create numeric features
# ------------------------
# Simple numeric features: number of tokens and number of characters
def feature_vector(text):
    tokens = text.split()
    return [len(tokens), len(text)]

X = np.array([feature_vector(t) for t in texts], dtype=np.float32)

# ------------------------
# 4️⃣ Build model
# ------------------------
model = tf.keras.Sequential([
    tf.keras.layers.Input(shape=(2,)),  # 2 numeric features
    tf.keras.layers.Dense(32, activation='relu'),  # slightly larger for improved capacity
    tf.keras.layers.Dense(16, activation='relu'),
    tf.keras.layers.Dense(4, activation='softmax')
])

model.compile(
    optimizer='adam',
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

# ------------------------
# 5️⃣ Train model
# ------------------------
model.fit(X, labels, epochs=120, verbose=2)  # slightly more epochs

# ------------------------
# 6️⃣ Convert to TFLite
# ------------------------
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
converter.inference_input_type = tf.float32
converter.inference_output_type = tf.float32
tflite_model = converter.convert()

with open("context_classifier.tflite", "wb") as f:
    f.write(tflite_model)

print("✅ TFLite model saved as context_classifier.tflite")