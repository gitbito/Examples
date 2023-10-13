import sys
import os
from transformers import Wav2Vec2Tokenizer, Wav2Vec2ForCTC
import librosa as lb
import torch

# Check if a filename was provided
if len(sys.argv) != 2:
    print("Usage: python your_script_name.py <path_to_audio_file>")
    sys.exit(1)

audio_file_path = sys.argv[1]

# Initialize the tokenizer
tokenizer = Wav2Vec2Tokenizer.from_pretrained('facebook/wav2vec2-base-960h')

# Initialize the model
model = Wav2Vec2ForCTC.from_pretrained('facebook/wav2vec2-base-960h')

# Read the sound file
waveform, rate = lb.load(audio_file_path, sr=16000)

# Tokenize the waveform
input_values = tokenizer(waveform, return_tensors='pt').input_values

# Retrieve logits from the model
logits = model(input_values).logits

# Take argmax value and decode into transcription
predicted_ids = torch.argmax(logits, dim=-1)
transcription = tokenizer.batch_decode(predicted_ids)

# Play the audio file
os.system(f"open \"{audio_file_path}\"")  # On macOS
# For Linux, you might use: os.system(f"xdg-open \"{audio_file_path}\"")
# For Windows, you might use: os.system(f"start \"{audio_file_path}\"")

print("\n\n\n\n------------------")
# Print the output
print(transcription)
print("\n\n\n\n------------------")
