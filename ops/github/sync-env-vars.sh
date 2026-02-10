#!/usr/bin/env bash
set -euo pipefail

# Copy GitHub Actions environment variables between environments.
# Usage:
#   REPO=yourssu/soongpt-backend \
#   SOURCE_ENV=dev \
#   TARGET_ENV=dev-backup \
#   HOST_URL_OVERRIDE=api.backup.soongpt.yourssu.com \
#   ./ops/github/sync-env-vars.sh

REPO="${REPO:-yourssu/soongpt-backend}"
SOURCE_ENV="${SOURCE_ENV:-dev}"
TARGET_ENV="${TARGET_ENV:-dev-backup}"
HOST_URL_OVERRIDE="${HOST_URL_OVERRIDE:-}"

# Ensure target environment exists
if ! gh api "repos/$REPO/environments/$TARGET_ENV" >/dev/null 2>&1; then
  gh api -X PUT "repos/$REPO/environments/$TARGET_ENV" >/dev/null
fi

# Copy all variables from source -> target
while IFS= read -r row; do
  name=$(echo "$row" | base64 -d | jq -r '.name')
  value=$(echo "$row" | base64 -d | jq -r '.value' | tr -d '\r')
  gh variable set "$name" --repo "$REPO" --env "$TARGET_ENV" --body "$value"
done < <(gh api "repos/$REPO/environments/$SOURCE_ENV/variables" --jq '.variables[] | @base64')

# Optional override for HOST_URL
if [ -n "$HOST_URL_OVERRIDE" ]; then
  gh variable set HOST_URL --repo "$REPO" --env "$TARGET_ENV" --body "$HOST_URL_OVERRIDE"
fi

echo "[DONE] Synced vars: $SOURCE_ENV -> $TARGET_ENV"
