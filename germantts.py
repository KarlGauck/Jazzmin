import torch
from TTS.api import TTS

# Get device
device = "cuda" if torch.cuda.is_available() else "cpu"

# Init TTS
tts = TTS("tts_models/deu/fairseq/vits").to(device)

# Run TTS
# ❗ Since this model is multi-lingual voice cloning model, we must set the target speaker_wav and language
# Text to speech list of amplitude values as output

# Text to speech to a file
tts.tts_to_file(
    text="Wenn ich das Geld hätte, dann würde ich mir das Koks so heftig reinschallern, oh mein Gott!",
    file_path="output.wav",
)
