# Aadhini Core Runtime (Prototype v0.1)

A standalone Termux runtime for Aadhini. The Android app talks to this service through the Platform adapter; Gemini credentials and AI orchestration stay outside the APK.

## What this prototype includes

- Aadhini's single, persistent Tamil-first companion identity.
- Separate behaviour profiles: Companion, Task, Work, Emotional, Guardian, BIK-E, TV Kids, TV Entertainment, TV Work and Avita Work.
- Gemini 2.5 Flash as the primary model and Gemini 3 Flash Preview as fallback.
- SQLite conversation history (both user and assistant turns).
- User-controlled saved memory categories: Recent, Important and Patterns.
- Keyword-based relevant-memory retrieval and pinned-memory priority.
- Local HTTP endpoints for health, chat, mode switching and memory.
- Standard-library-only Python runtime; no pip packages required.

## Runtime layout on Android / Termux

~~~text
~/aadhini_core/
├── app/
│   └── aadhini_engine.py
├── data/
│   └── aadhini.db
└── aadhini.log

~/.aadhini_secrets/
└── env
~~~

Copy backend/aadhini_engine.py from this repository to ~/aadhini_core/app/aadhini_engine.py.

## Termux setup

~~~bash
mkdir -p ~/aadhini_core/app ~/aadhini_core/data ~/.aadhini_secrets
chmod 700 ~/.aadhini_secrets
nano ~/.aadhini_secrets/env
~~~

Put the key in the file (do not commit or share it):

~~~text
GEMINI_API_KEY=YOUR_REAL_KEY
~~~

Protect it:

~~~bash
chmod 600 ~/.aadhini_secrets/env
~~~

Start the engine:

~~~bash
python ~/aadhini_core/app/aadhini_engine.py
~~~

Check it from another Termux session:

~~~bash
curl -sS http://127.0.0.1:8766/health
~~~

## API

### GET /health

Returns runtime status, active mode and whether a Gemini key is configured. It never returns the key.

### POST /chat

~~~json
{"message":"Vanakkam Aadhini"}
~~~

Returns reply, mode, selected model, latency_ms and memory_used.

### POST /mode/switch

~~~json
{"mode":"work"}
~~~

Supported modes: companion, task, work, emotional, guardian, bike, tv_kids, tv_entertainment, tv_work, avita_work.

### POST /memory/add

~~~json
{"category":"Important","content":"Aadhini is Tamil-first","pinned":true}
~~~

Categories: Recent, Important, Patterns.

### GET /memory/list

Returns saved memory cards for review.

## Design and privacy boundaries

- API keys stay in Termux, not in Android BuildConfig or the APK.
- The server binds only to 127.0.0.1; it is not exposed to the Wi-Fi network.
- Conversation history is stored locally in SQLite.
- Only relevant saved memories are included in a model request.
- This prototype does not scan the phone's files or family/vault folders.
- Saved memory is separate from conversation history. Do not treat every chat as a permanent fact.
- This is a prototype, not a claim of production-grade encryption or biometric authorization. Protected-memory authorization must be connected to the Android security flow before sensitive memory is exposed.

## Performance notes

The runtime calls one primary model first and tries one fallback only if the primary request fails. It records end-to-end model latency in the response. A 3–5 second response cannot be guaranteed: model load, network quality, rate limits and response length still matter. Streaming can be added after the non-streaming API is validated end-to-end.

## Current prototype status

The Python runtime and Platform HTTP adapter are implemented as a separate prototype branch. Android UI integration, startup orchestration, real-device testing and streaming are follow-up steps. Do not merge this branch into main until those checks pass.
