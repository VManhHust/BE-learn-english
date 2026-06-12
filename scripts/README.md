# Python Scripts Setup

## Prerequisites

- Python 3.7 or higher
- pip (Python package manager)

## Installation

1. Install Python dependencies:

```bash
cd scripts
pip install -r requirements.txt
```

Or using pip3:

```bash
pip3 install -r requirements.txt
```

2. Test the script manually:

```bash
python3 download_transcript.py dQw4w9WgXcQ
```

Expected output (JSON):
```json
{
  "success": true,
  "video_id": "dQw4w9WgXcQ",
  "language": "en",
  "segments": [
    {
      "id": 1,
      "t_start_ms": 0,
      "t_end_ms": 5000,
      "caption": "Never gonna give you up"
    },
    ...
  ]
}
```

## Configuration

The Java application can be configured via environment variables or application.properties:

- `PYTHON_COMMAND`: Python command (default: `python3`)
- `PYTHON_SCRIPT_PATH`: Path to script (default: `scripts/download_transcript.py`)
- `PYTHON_SCRIPT_TIMEOUT`: Timeout in seconds (default: `30`)

## Troubleshooting

### Python not found

If you get "python3: command not found", try:
- Windows: Use `python` instead of `python3`
- Mac/Linux: Install Python 3: `brew install python3` or `apt-get install python3`

### Module not found

If you get "No module named 'youtube_transcript_api'":
```bash
pip3 install youtube-transcript-api
```

### Permission denied

On Linux/Mac, make the script executable:
```bash
chmod +x download_transcript.py
```
