#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./run_major_category_api_validation.sh [schoolId]
# Example:
#   ./run_major_category_api_validation.sh 25

SCHOOL_ID="${1:-25}"
BASE_URL="https://api.dev.soongpt.yourssu.com"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

python3 -u "$SCRIPT_DIR/validate_major_category_api.py" \
  --base-url "$BASE_URL" \
  --school-id "$SCHOOL_ID" \
  --use-curl
