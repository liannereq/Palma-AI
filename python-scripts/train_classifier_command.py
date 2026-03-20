import numpy as np
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.callbacks import EarlyStopping
import pickle

print("🚀 Starting training for command classifier...")

np.random.seed(42)
tf.random.set_seed(42)

# ------------------------
# 1️⃣ Dataset
# ------------------------
texts = []
labels_text = []

def add(samples, label):
    texts.extend(samples)
    labels_text.extend([label]*len(samples))

# Example commands (expand for accuracy)
add([
    "show my lists","create a new list","add milk to list","remove eggs from shopping list",
    "set a reminder","remind me at 7 am","delete reminder",
    "add a contact named john","delete contact mike",
    "do something","help me"
], "list")  # Repeat for reminder, contact, default...

# ------------------------
# 2️⃣ Encode labels
# ------------------------
label_map = {"list":0,"reminder":1,"contact":2,"default":3}
labels = np.array([label_map[l] for l in labels_text])
labels = to_categorical(labels, num_classes=4)

# ------------------------
# 3️⃣ Feature extraction (token count, char count, keyword flags)
# ------------------------
list_keywords = {"list","grocery","shopping","task","add","remove","delete","load","create","make","open"}
reminder_keywords = {"remind","reminder","daily","weekly","monthly","yearly","set","cancel","alarm","notify","wake","schedule"}
contact_keywords = {"contact","email","group","user","ai","save","delete","remove","add"}

def extract_features(texts):
    features = []
    for t in texts:
        t_lower = t.lower()
        tokens = t_lower.split()
        count_tokens = len(tokens)
        count_chars = len(t_lower)
        list_flag = 1.0 if any(tok in list_keywords for tok in tokens) else 0.0
        reminder_flag = 1.0 if any(tok in reminder_keywords for tok in tokens) else 0.0
        contact_flag = 1.0 if any(tok in contact_keywords for tok in tokens) else 0.0
        features.append([count_tokens, count_chars, list_flag, reminder_flag, contact_flag])
    return np.array(features, dtype=np.float32)

input_features = extract_features(texts)

# ------------------------
# 4️⃣ Build simple fully connected model
# ------------------------
model = Sequential([
    Dense(64, activation='relu', input_shape=(5,)),
    Dropout(0.3),
    Dense(32, activation='relu'),
    Dense(4, activation='softmax')
])

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
model.summary()

# ------------------------
# 5️⃣ Train model with early stopping and class weights
# ------------------------
class_counts = np.sum(labels, axis=0)
total_samples = labels.shape[0]
class_weights = {i: total_samples/(4*count) for i, count in enumerate(class_counts)}

early_stop = EarlyStopping(monitor='val_accuracy', patience=10, restore_best_weights=True)

model.fit(
    input_features, labels,
    epochs=200,
    verbose=2,
    validation_split=0.15,
    class_weight=class_weights,
    callbacks=[early_stop]
)

print("✅ Training complete")

# ------------------------
# 6️⃣ Convert to TFLite
# ------------------------
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open("command_classifier.tflite", "wb") as f:
    f.write(tflite_model)
print("✅ TFLite model saved successfully!")

# ------------------------
# 7️⃣ Save tokenizer (optional)
# ------------------------
with open("command_tokenizer.pkl", "wb") as f:
    pickle.dump(texts, f)
print("✅ Tokenizer saved (for reference)")