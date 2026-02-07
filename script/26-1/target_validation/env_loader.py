from __future__ import annotations

import os
from pathlib import Path


def load_api_key(env_path: str, key_name: str = "gemini_api_key") -> str:
    key_from_env = os.getenv(key_name) or os.getenv(key_name.upper())
    if key_from_env:
        return key_from_env.strip()

    env_file = Path(env_path)
    if not env_file.exists():
        return ""

    for raw_line in env_file.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        if key.strip().lower() != key_name.lower():
            continue
        return value.strip().strip("'").strip('"')
    return ""
