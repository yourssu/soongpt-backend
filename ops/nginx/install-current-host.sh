#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   SERVER_NAME=api.example.com SERVER_PORT=8080 ./ops/nginx/install-current-host.sh
# Defaults:
#   SERVER_NAME=_
#   SERVER_PORT=8080

SERVER_NAME="${SERVER_NAME:-_}"
SERVER_PORT="${SERVER_PORT:-8080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_PATH="$SCRIPT_DIR/soongpt.current-host.conf.template"
OUTPUT_PATH="/etc/nginx/sites-available/soongpt"
ENABLED_PATH="/etc/nginx/sites-enabled/soongpt"

if ! command -v envsubst >/dev/null 2>&1; then
  echo "[INFO] envsubst not found. Installing gettext-base..."
  sudo apt-get update
  sudo apt-get install -y gettext-base
fi

if ! command -v nginx >/dev/null 2>&1; then
  echo "[INFO] nginx not found. Installing nginx..."
  sudo apt-get update
  sudo apt-get install -y nginx
fi

echo "[INFO] Rendering nginx config (SERVER_NAME=$SERVER_NAME, SERVER_PORT=$SERVER_PORT)"
envsubst '${SERVER_NAME} ${SERVER_PORT}' < "$TEMPLATE_PATH" | sudo tee "$OUTPUT_PATH" >/dev/null

# Disable default site to avoid conflicts
if [ -f /etc/nginx/sites-enabled/default ]; then
  sudo rm -f /etc/nginx/sites-enabled/default
fi

sudo ln -sfn "$OUTPUT_PATH" "$ENABLED_PATH"

sudo nginx -t
sudo systemctl enable nginx
sudo systemctl reload nginx

echo "[DONE] nginx is configured and reloaded."
