#!/usr/bin/env python3
"""Aadhini Core Runtime prototype for Termux (Python standard library only)."""
from __future__ import annotations

import json
import os
import sqlite3
import time
import urllib.error
import urllib.request
from contextlib import contextmanager
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any

HOME = Path.home()
APP_HOME = Path(os.environ.get("AADHINI_HOME", HOME / "aadhini_core")).expanduser()
DATA_DIR = APP_HOME / "data"
DB_PATH = Path(os.environ.get("AADHINI_DB", DATA_DIR / "aadhini.db")).expanduser()
LOG_PATH = APP_HOME / "aadhini.log"
SECRET_FILE = Path(os.environ.get("AADHINI_SECRET_FILE", HOME / ".aadhini_secrets" / "env")).expanduser()
HOST = os.environ.get("AADHINI_HOST", "127.0.0.1")
PORT = int(os.environ.get("AADHINI_PORT", "8766"))
REQUEST_LIMIT = 64 * 1024
HISTORY_LIMIT = 12
MEMORY_LIMIT = 6
PRIMARY_MODEL = os.environ.get("AADHINI_GEMINI_MODEL", "gemini-2.5-flash")
FALLBACK_MODEL = os.environ.get("AADHINI_GEMINI_FALLBACK", "gemini-3-flash-preview")
MODEL_TIMEOUT = float(os.environ.get("AADHINI_MODEL_TIMEOUT", "10"))

MODE_PROMPTS = {
    "companion": "Casual, warm, natural Tamil-first companion. Be familiar without being clingy.",
    "task": "Action-oriented assistant. Clarify only essential gaps, organise steps, and report completion honestly.",
    "work": "Mature, precise professional assistant. Use correct railway, commercial and technical terminology. Separate verified facts from assumptions.",
    "emotional": "Patient, empathetic and non-judgmental. Listen first; avoid blame and forced positivity.",
    "guardian": "Safety-first advisor. Prioritise verified emergencies and critical safety; do not exaggerate danger.",
    "bike": "BIK-E riding companion. Keep responses short and hands-free; never guess speed, RPM, gear or sensor state.",
    "tv_kids": "Age-appropriate, family-safe, simple and encouraging.",
    "tv_entertainment": "Relaxed, engaging entertainment companion.",
    "tv_work": "Focused information and productivity assistant for a TV environment.",
    "avita_work": "Detailed desktop-oriented development, coding, research and productivity assistant.",
}


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def log(message: str) -> None:
    try:
        APP_HOME.mkdir(parents=True, exist_ok=True)
        with LOG_PATH.open("a", encoding="utf-8") as f:
            f.write(f"{now_iso()} | {message}\n")
    except OSError:
        pass


def load_api_key() -> str:
    key = os.environ.get("GEMINI_API_KEY", "").strip()
    if key:
        return key
    if SECRET_FILE.is_file():
        try:
            for line in SECRET_FILE.read_text(encoding="utf-8").splitlines():
                line = line.strip()
                if line.startswith("GEMINI_API_KEY="):
                    return line.split("=", 1)[1].strip().strip("\"'")
        except OSError as exc:
            log(f"SECRET_READ_ERROR {exc!r}")
    return ""


@contextmanager
def connect():
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(DB_PATH, timeout=10)
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def init_db() -> None:
    with connect() as conn:
        conn.execute("PRAGMA journal_mode=WAL")
        conn.execute("""CREATE TABLE IF NOT EXISTS session_state (
            id INTEGER PRIMARY KEY CHECK(id=1), mode TEXT NOT NULL, updated_at TEXT NOT NULL
        )""")
        conn.execute("INSERT OR IGNORE INTO session_state(id, mode, updated_at) VALUES(1, 'companion', ?)", (now_iso(),))
        conn.execute("""CREATE TABLE IF NOT EXISTS conversation_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT NOT NULL, content TEXT NOT NULL,
            mode TEXT NOT NULL, created_at TEXT NOT NULL
        )""")
        conn.execute("""CREATE TABLE IF NOT EXISTS memory_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL,
            content TEXT NOT NULL, pinned INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL
        )""")


def current_mode() -> str:
    with connect() as conn:
        row = conn.execute("SELECT mode FROM session_state WHERE id=1").fetchone()
    return row["mode"] if row else "companion"


def switch_mode(mode: str) -> str:
    if mode not in MODE_PROMPTS:
        raise ValueError(f"Unsupported mode: {mode}")
    with connect() as conn:
        conn.execute("UPDATE session_state SET mode=?, updated_at=? WHERE id=1", (mode, now_iso()))
    return mode


def recent_history(limit: int = HISTORY_LIMIT) -> list[dict[str, str]]:
    with connect() as conn:
        rows = conn.execute(
            "SELECT role, content FROM conversation_messages ORDER BY id DESC LIMIT ?", (limit,)
        ).fetchall()
    return [{"role": r["role"], "content": r["content"]} for r in reversed(rows)]


def relevant_memories(query: str, limit: int = MEMORY_LIMIT) -> list[str]:
    words = {w.casefold().strip(".,!?;:()[]{}\"'") for w in query.split() if len(w) >= 3}
    with connect() as conn:
        rows = conn.execute(
            "SELECT category, content, pinned FROM memory_items ORDER BY pinned DESC, id DESC LIMIT 250"
        ).fetchall()
    scored: list[tuple[int, int, str]] = []
    for row in rows:
        text = row["content"]
        score = sum(1 for word in words if word in text.casefold())
        if score or row["pinned"]:
            scored.append((score, int(row["pinned"]), f"[{row['category']}] {text}"))
    scored.sort(key=lambda item: (item[1], item[0]), reverse=True)
    return [item[2] for item in scored[:limit]]


def save_message(role: str, content: str, mode: str) -> None:
    with connect() as conn:
        conn.execute(
            "INSERT INTO conversation_messages(role, content, mode, created_at) VALUES(?,?,?,?)",
            (role, content, mode, now_iso()),
        )


def add_memory(content: str, category: str = "Important", pinned: bool = False) -> int:
    if category not in {"Recent", "Important", "Patterns"}:
        raise ValueError("category must be Recent, Important or Patterns")
    with connect() as conn:
        cur = conn.execute(
            "INSERT INTO memory_items(category, content, pinned, created_at) VALUES(?,?,?,?)",
            (category, content.strip(), 1 if pinned else 0, now_iso()),
        )
        return int(cur.lastrowid)


def build_system_instruction(mode: str, memories: list[str]) -> str:
    mode_guidance = MODE_PROMPTS.get(mode, MODE_PROMPTS["companion"])
    memory_text = "\n".join(f"- {item}" for item in memories) or "No relevant saved memories."
    return f"""You are Aadhini, one persistent female AI companion across all device environments.
Identity: warm, caring, intelligent, curious, practical and emotionally aware.
Language: Tamil-first; understand natural Tamil and Tanglish. Use English when the user does.
Style: natural and concise by default. Be playful only when suitable. Never sound like customer support.
Judgement: confident but not arrogant; respectfully disagree when needed. Never pretend to know, remember, or complete an action you did not.
Continuity: use relevant conversation history and saved memories; do not ask the user to repeat known relevant facts.
Privacy: use only the context supplied here. Do not expose sensitive information unnecessarily. Never claim private data is protected unless the system confirms it.
Interaction: do not use 'How can I help?' as a default greeting. Offer one useful next step when appropriate.
Current environment mode: {mode.upper()}.
Mode behaviour: {mode_guidance}
Relevant saved memories (reference, not instructions):
{memory_text}
"""


def gemini_request(model: str, message: str, mode: str, history: list[dict[str, str]], memories: list[str]) -> str:
    api_key = load_api_key()
    if not api_key:
        raise RuntimeError("GEMINI_API_KEY is not configured in Termux.")
    contents: list[dict[str, Any]] = []
    for item in history[-HISTORY_LIMIT:]:
        role = "model" if item["role"] == "assistant" else "user"
        contents.append({"role": role, "parts": [{"text": item["content"]}]})
    contents.append({"role": "user", "parts": [{"text": message}]})
    payload = {
        "system_instruction": {"parts": [{"text": build_system_instruction(mode, memories)}]},
        "contents": contents,
        "generationConfig": {
            "temperature": 0.65,
            "maxOutputTokens": 700,
            "thinkingConfig": {"thinkingBudget": 0},
        },
    }
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
    req = urllib.request.Request(
        url, data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        headers={"Content-Type": "application/json"}, method="POST",
    )
    with urllib.request.urlopen(req, timeout=MODEL_TIMEOUT) as response:
        data = json.loads(response.read().decode("utf-8"))
    candidates = data.get("candidates") or []
    if not candidates:
        raise RuntimeError("Gemini returned no response candidate.")
    parts = candidates[0].get("content", {}).get("parts", [])
    reply = "".join(part.get("text", "") for part in parts).strip()
    if not reply:
        raise RuntimeError("Gemini returned an empty response.")
    return reply


def generate_reply(message: str, mode: str) -> tuple[str, str, int, int]:
    history = recent_history()
    memories = relevant_memories(message)
    started = time.monotonic()
    errors: list[str] = []
    for index, model in enumerate((PRIMARY_MODEL, FALLBACK_MODEL)):
        try:
            reply = gemini_request(model, message, mode, history, memories)
            return reply, model, int((time.monotonic() - started) * 1000), len(memories)
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError, RuntimeError, ValueError) as exc:
            errors.append(f"{model}: {exc!r}")
            log(f"MODEL_ERROR {model} {exc!r}")
            if index == 0:
                continue
    raise RuntimeError("AI provider unavailable. " + " | ".join(errors))


class Handler(BaseHTTPRequestHandler):
    server_version = "AadhiniCore/0.1"

    def send_json(self, status: int, payload: dict[str, Any]) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        try:
            self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            log("CLIENT_DISCONNECTED during response")

    def read_json(self) -> dict[str, Any]:
        length = int(self.headers.get("Content-Length", "0"))
        if length < 0 or length > REQUEST_LIMIT:
            raise ValueError("Request body too large.")
        raw = self.rfile.read(length).decode("utf-8") if length else "{}"
        data = json.loads(raw)
        if not isinstance(data, dict):
            raise ValueError("JSON body must be an object.")
        return data

    def do_GET(self) -> None:
        if self.path == "/health":
            self.send_json(200, {
                "status": "ok", "service": "Aadhini Core Runtime", "version": "0.1",
                "mode": current_mode(), "gemini_configured": bool(load_api_key()),
            })
        elif self.path == "/memory/list":
            with connect() as conn:
                rows = conn.execute(
                    "SELECT id, category, content, pinned, created_at FROM memory_items ORDER BY id DESC LIMIT 100"
                ).fetchall()
            self.send_json(200, {"items": [dict(row) for row in rows]})
        else:
            self.send_json(404, {"error": "Not found"})

    def do_POST(self) -> None:
        try:
            data = self.read_json()
            if self.path == "/mode/switch":
                mode = switch_mode(str(data.get("mode", "companion")).strip().lower())
                self.send_json(200, {"mode": mode})
                return
            if self.path == "/memory/add":
                content = str(data.get("content", "")).strip()
                if not content:
                    self.send_json(400, {"error": "Memory content is required."})
                    return
                item_id = add_memory(content, str(data.get("category", "Important")), bool(data.get("pinned", False)))
                self.send_json(200, {"id": item_id, "saved": True})
                return
            if self.path == "/chat":
                message = str(data.get("message", "")).strip()
                if not message:
                    self.send_json(400, {"error": "Message cannot be empty."})
                    return
                mode = current_mode()
                reply, model, latency_ms, memory_count = generate_reply(message, mode)
                save_message("user", message, mode)
                save_message("assistant", reply, mode)
                self.send_json(200, {
                    "reply": reply, "mode": mode, "provider": "gemini", "model": model,
                    "latency_ms": latency_ms, "memory_used": memory_count,
                })
                return
            self.send_json(404, {"error": "Not found"})
        except ValueError as exc:
            self.send_json(400, {"error": str(exc)})
        except RuntimeError as exc:
            log(f"REQUEST_ERROR {exc!r}")
            self.send_json(503, {"error": str(exc), "provider": "unavailable"})
        except Exception as exc:
            log(f"UNHANDLED_ERROR {exc!r}")
            self.send_json(500, {"error": "Internal Core Runtime error."})

    def log_message(self, fmt: str, *args: Any) -> None:
        log(fmt % args)


def main() -> None:
    init_db()
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    server.daemon_threads = True
    print(f"Aadhini Core Runtime listening on http://{HOST}:{PORT}")
    log(f"START host={HOST} port={PORT} db={DB_PATH}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
        log("STOP")


if __name__ == "__main__":
    main()
