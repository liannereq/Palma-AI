import numpy as np
import tensorflow as tf
import pickle
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Embedding, GlobalAveragePooling1D, Dense
from tensorflow.keras.utils import to_categorical

print("🚀 Starting training for query type classifier...")

# ------------------------
# 1️⃣ Query Dataset
# ------------------------
texts = [
    # 🔹 USER QUERIES (about someone's personal info)
    "what is my username",
    "show my email",
    "what is my phone number",
    "tell me my address",
    "when is my birthday",
    "what is my gender",
    "give me my contact info",
    "show my mobile number",

   # 🔹 AI PERSONAL INFO-LIKE QUERIES (similar to user fields)
   "what is the AI's name",
   "show AI's email",
   "what is the AI's contact number",
   "tell me the AI's address",
   "when was the AI created",
   "what is the AI's gender",
   "show AI's mobile number",

    # 🔹 LOG QUERIES (based on stored message logs, generic)
    "tell me about cats",
    "what do you know about dogs",
    "give me information on quantum physics",
    "what do you know about Python programming",
    "show details about the recipe",
    "what was the last fact I mentioned",
    "what do you know about basketball",
    "do you remember movies we talked about",
]

labels_text = (
    ["user"] * 8 +
    ["ai"] * 7 +
    ["log"] * 8
)

# ------------------------
# 2️⃣ Label Encoding
# ------------------------
label_map = {"user": 0, "ai": 1, "log": 2}
labels = np.array([label_map[l] for l in labels_text])
labels = to_categorical(labels, num_classes=3)

# ------------------------
# 3️⃣ Tokenize & Pad
# ------------------------
vocab_size = 1500
max_length = 12
embedding_dim = 32

# Lowercase for normalization
texts = [t.lower() for t in texts]

tokenizer = Tokenizer(num_words=vocab_size, oov_token="<OOV>")
tokenizer.fit_on_texts(texts)

sequences = tokenizer.texts_to_sequences(texts)
padded = pad_sequences(sequences, maxlen=max_length, padding='post')

# Save tokenizer for app
with open("type_query_tokenizer.pkl", "wb") as f:
    pickle.dump(tokenizer, f)

print("✅ Tokenizer saved")

# ------------------------
# 4️⃣ Build Model
# ------------------------
model = Sequential([
    Embedding(vocab_size, embedding_dim, input_length=max_length),
    GlobalAveragePooling1D(),
    Dense(32, activation='relu'),
    Dense(16, activation='relu'),
    Dense(3, activation='softmax')  # 3 output types: user, ai, log
])

model.compile(
    loss='categorical_crossentropy',
    optimizer='adam',
    metrics=['accuracy']
)

model.build(input_shape=(None, max_length))
model.summary()

# ------------------------
# 5️⃣ Train Model
# ------------------------
model.fit(padded, labels, epochs=80, verbose=2)

print("✅ Training complete")

# ------------------------
# 6️⃣ Convert to TFLite
# ------------------------
print("🔄 Converting to TFLite...")

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]

# ⚠️ DO NOT force inference_input_type to int32 for Embedding layers
# Leave default (float32)
tflite_model = converter.convert()

with open("type_query_classifier.tflite", "wb") as f:
    f.write(tflite_model)

print("✅ TFLite model saved successfully!")
print("✅ Tokenizer saved as type_query_tokenizer.pkl")