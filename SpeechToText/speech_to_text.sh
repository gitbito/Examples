#!/bin/bash

# Check if a filename was provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <path_to_audio_file>"
    exit 1
fi

audio_file_path="$1"

# Call the Python script and pipe its output to bito
transcription=$(python3 speech_to_text.py "$audio_file_path")
echo "$transcription" | bito
