#!/usr/bin/env bash
set -euo pipefail

VALIDATOR_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="${1:-$(cd "$VALIDATOR_DIR/../../.." && pwd)}"
CSV_PATH="${2:-$ROOT_DIR/script/26-1/course/ssu26-1.csv}"
ENV_PATH="${3:-$ROOT_DIR/.env}"
DATA_YML_PATH="${4:-$ROOT_DIR/src/main/resources/data.yml}"

echo "[1/5] Unit tests"
python3 -m unittest discover -s "$VALIDATOR_DIR/tests" -p 'test_*.py' -v

echo "[2/5] Offline validation (rules only)"
python3 "$VALIDATOR_DIR/run_validation.py" \
  --csv-path "$CSV_PATH" \
  --data-yml-path "$DATA_YML_PATH" \
  --env-path "$ENV_PATH" \
  --report-path "$VALIDATOR_DIR/reports/validation_report_offline.json" \
  --manual-review-path "$VALIDATOR_DIR/reports/manual_review_offline.csv" \
  --disable-ai

echo "[3/5] E2E validation (rules + Gemini)"
python3 "$VALIDATOR_DIR/run_validation.py" \
  --csv-path "$CSV_PATH" \
  --data-yml-path "$DATA_YML_PATH" \
  --env-path "$ENV_PATH" \
  --gemini-model gemini-flash-3.0 \
  --ai-confidence-threshold 0.8 \
  --report-path "$VALIDATOR_DIR/reports/validation_report_e2e.json" \
  --manual-review-path "$VALIDATOR_DIR/reports/manual_review_e2e.csv"

echo "[4/5] Deployment gate validation"
python3 "$VALIDATOR_DIR/run_validation.py" \
  --csv-path "$CSV_PATH" \
  --data-yml-path "$DATA_YML_PATH" \
  --env-path "$ENV_PATH" \
  --gemini-model gemini-flash-3.0 \
  --ai-confidence-threshold 0.8 \
  --fail-on-manual-review \
  --fail-on-ai-skipped \
  --require-ai-invoked \
  --min-coverage-rate 99 \
  --report-path "$VALIDATOR_DIR/reports/validation_report_gate.json" \
  --manual-review-path "$VALIDATOR_DIR/reports/manual_review_gate.csv"

echo "[5/5] Completed"
