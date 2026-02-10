#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   SERVER_NAME=api.backup.soongpt.yourssu.com \
#   SERVER_PORT=9001 \
#   LETSENCRYPT_EMAIL=you@example.com \
#   ./ops/nginx/enable-https-letsencrypt.sh

SERVER_NAME="${SERVER_NAME:-}"
SERVER_PORT="${SERVER_PORT:-8080}"
LETSENCRYPT_EMAIL="${LETSENCRYPT_EMAIL:-}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -z "$SERVER_NAME" ]; then
  echo "[ERROR] SERVER_NAME is required (e.g. api.backup.soongpt.yourssu.com)"
  exit 1
fi

if [ -z "$LETSENCRYPT_EMAIL" ]; then
  echo "[ERROR] LETSENCRYPT_EMAIL is required for Let's Encrypt registration"
  exit 1
fi

if [ "$SERVER_NAME" = "_" ]; then
  echo "[ERROR] SERVER_NAME cannot be '_' when issuing TLS certificate"
  exit 1
fi

echo "[1/4] Install & apply HTTP nginx reverse proxy first"
SERVER_NAME="$SERVER_NAME" SERVER_PORT="$SERVER_PORT" "$SCRIPT_DIR/install-current-host.sh"

echo "[2/4] Install certbot nginx plugin"
sudo apt-get update
sudo apt-get install -y certbot python3-certbot-nginx

echo "[3/4] Issue certificate and force HTTPS redirect"
sudo certbot --nginx \
  -d "$SERVER_NAME" \
  -m "$LETSENCRYPT_EMAIL" \
  --agree-tos \
  --no-eff-email \
  --redirect \
  --non-interactive

echo "[4/4] Validate nginx config and reload"
sudo nginx -t
sudo systemctl reload nginx

echo "[DONE] HTTPS is enabled for https://$SERVER_NAME"
echo "[INFO] Auto-renew is managed by certbot timer (systemd)."
