`SpeechToText` POC: Demo Video : https://youtu.be/v9CObQff10o

---

# SpeechToText

Convert your audio files to text easily with the `SpeechToText` project. The project uses Hugging Face's Wav2Vec2 model to perform the speech-to-text operation. It also offers a shell script to process the generated text using `bito`, a CLI tool.

## Table of Contents
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Support](#support)

## Requirements

- Python 3
- Bash (for using the shell script)
- Python packages: `transformers`, `librosa`, `torch`

## Installation

1. **Clone the Repository**:
    ```bash
    git clone [Your Repository Link]
    cd [Your Repository Folder Name]
    ```

2. **Install Required Python Packages**:
    ```bash
    pip3 install transformers librosa torch
    ```

## Usage

### Using Python Script

1. **Convert an audio file to text**:
    ```bash
    python3 speech_to_text.py [path_to_audio_file]
    ```
    Example:
    ```bash
    python3 speech_to_text.py Read_in_a_file_print_to_console.m4a
    ```

### Using Shell Script

1. **Convert an audio file to text and process with `bito`**:
    ```bash
    chmod +x speech_to_text.sh
    ./speech_to_text.sh [path_to_audio_file]
    ```
    Example:
    ```bash
    ./speech_to_text.sh Read_in_a_file_print_to_console.m4a
    ```

Note: Ensure that `bitcli` is properly installed and available in your system's PATH for the shell script to work as expected.

## Support

For issues, feature requests, or assistance, please open an issue in the repository.

---

