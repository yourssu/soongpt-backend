#!/usr/bin/env python3
"""Compare ECC downloaded course '수강대상' vs API course.target.

Inputs:
- CSVs under script/26-1/downloads_csv (converted from ECC xls downloads)
- API base URL (default: https://api.dev.soongpt.yourssu.com)

Outputs:
- Markdown report summarizing mismatches.

Notes:
- ECC CSV rows may contain multiple lines; use Python csv module with newline=''.
- Normalization: collapse whitespace.
"""

from __future__ import annotations

import csv
import datetime as dt
import json
import os
import re
import sys
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Tuple


API_BASE = os.environ.get("API_BASE", "https://api.dev.soongpt.yourssu.com").rstrip("/")
CSV_DIR = Path(os.environ.get("CSV_DIR", "/home/ubuntu/soongpt-backend/script/26-1/downloads_csv"))
OUT_PATH = Path(
    os.environ.get(
        "OUT_PATH",
        f"/home/ubuntu/soongpt-backend/script/26-1/docs/target_parser/ECC다운로드_vs_API_수강대상_대조보고서_{dt.datetime.now().strftime('%Y%m%d_%H%M%S')}.md",
    )
)

CODE_COL = "과목번호"
TARGET_COL = "수강대상"
NAME_COL = "과목명"
DEPT_COL = "개설학과"

_ws_re = re.compile(r"\s+")
_paren_dup_re = re.compile(r"(\([^)]*\))(?:\1)+")


def norm(s: str | None) -> str:
    """Light normalization: collapse whitespace."""
    if s is None:
        return ""
    s = s.replace("\u00a0", " ")
    s = _ws_re.sub(" ", s)
    return s.strip()


def norm_semantic(s: str | None) -> str:
    """Semantic normalization for target strings.

    Goal: treat formatting noise as equal.
    - normalize whitespace
    - normalize comma spacing
    - collapse duplicated parenthetical markers e.g. (대상외수강제한)(대상외수강제한)
    - collapse repeated identical parenthetical groups in general
    """
    s = norm(s)
    if not s:
        return ""

    # unify comma spacing: "A, B" (also handles missing spaces like ",B")
    s = re.sub(r"\s*,\s*", ", ", s)

    # collapse duplicated parenthetical groups anywhere
    # repeatedly apply until stable (to handle triple repeats)
    prev = None
    while prev != s:
        prev = s
        s = _paren_dup_re.sub(r"\1", s)

    # very common noisy repeat: 대상외수강제한 appearing twice with optional spaces
    s = re.sub(r"\(대상외수강제한\)\s*\(대상외수강제한\)", "(대상외수강제한)", s)

    return s.strip()


@dataclass
class Row:
    code: int
    name: str
    dept: str
    ecc_target: str


def iter_rows(csv_path: Path) -> Iterable[Row]:
    with csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        missing = [c for c in [CODE_COL, TARGET_COL, NAME_COL, DEPT_COL] if c not in (reader.fieldnames or [])]
        if missing:
            raise RuntimeError(f"Missing columns in {csv_path.name}: {missing}. Have: {reader.fieldnames}")
        for r in reader:
            code_raw = norm(r.get(CODE_COL))
            if not code_raw:
                continue
            try:
                code = int(code_raw)
            except ValueError:
                continue
            yield Row(
                code=code,
                name=norm(r.get(NAME_COL)),
                dept=norm(r.get(DEPT_COL)),
                ecc_target=norm(r.get(TARGET_COL)),
            )


def api_fetch_targets(codes: List[int]) -> Dict[int, Dict]:
    """Return mapping code -> course detail object (as returned by /api/courses)."""
    # build query with repeated code params
    params = []
    for c in codes:
        params.append(("code", str(c)))
    url = f"{API_BASE}/api/courses?" + urllib.parse.urlencode(params)

    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode("utf-8"))

    result = data.get("result") or []
    out: Dict[int, Dict] = {}
    for item in result:
        try:
            out[int(item.get("code"))] = item
        except Exception:
            continue
    return out


def chunked(xs: List[int], n: int) -> Iterable[List[int]]:
    for i in range(0, len(xs), n):
        yield xs[i : i + n]


def main() -> int:
    csv_files = sorted([p for p in CSV_DIR.glob("*.csv") if p.is_file()])
    if not csv_files:
        print(f"No csv files in {CSV_DIR}", file=sys.stderr)
        return 2

    # load ECC targets per file
    per_file: Dict[str, Dict[int, Row]] = {}
    for p in csv_files:
        rows: Dict[int, Row] = {}
        for row in iter_rows(p):
            # if same code appears multiple times (multiple divisions), keep the most informative target (longest)
            prev = rows.get(row.code)
            if prev is None or len(row.ecc_target) > len(prev.ecc_target):
                rows[row.code] = row
        per_file[p.name] = rows

    all_codes = sorted({code for rows in per_file.values() for code in rows.keys()})

    # fetch API data in chunks
    api_map: Dict[int, Dict] = {}
    for chunk in chunked(all_codes, 60):
        api_map.update(api_fetch_targets(chunk))

    # compare
    total_codes = len(all_codes)
    missing_in_api = []
    mismatches = []  # (file, code, name, ecc_target, api_target)

    for fname, rows in per_file.items():
        for code, row in rows.items():
            api_item = api_map.get(code)
            if api_item is None:
                missing_in_api.append((fname, code, row.name))
                continue
            api_target = norm(api_item.get("target"))
            # Compare using semantic normalization (ignore formatting noise)
            ecc_n = norm_semantic(row.ecc_target)
            api_n = norm_semantic(api_target)

            # If one side looks like a truncated prefix of the other (LibreOffice CSV conversion may truncate long cells),
            # treat as equivalent.
            short, long = (ecc_n, api_n) if len(ecc_n) <= len(api_n) else (api_n, ecc_n)
            is_trunc_prefix = len(short) >= 30 and long.startswith(short)

            if ecc_n != api_n and not is_trunc_prefix:
                mismatches.append((fname, code, row.name, row.ecc_target, api_target))

    # summary per file
    per_file_stats = []
    for fname, rows in per_file.items():
        codes = list(rows.keys())
        miss = sum(1 for c in codes if c not in api_map)
        def is_mm(c: int) -> bool:
            ecc_n = norm_semantic(rows[c].ecc_target)
            api_n = norm_semantic(api_map[c].get("target"))
            if ecc_n == api_n:
                return False
            short, long = (ecc_n, api_n) if len(ecc_n) <= len(api_n) else (api_n, ecc_n)
            if len(short) >= 30 and long.startswith(short):
                return False
            return True

        mm = sum(1 for c in codes if (c in api_map and is_mm(c)))
        per_file_stats.append((fname, len(rows), miss, mm))

    # write report
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    now = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    lines = []
    lines.append(f"# ECC 다운로드 vs API 수강대상(target) 대조 보고서\n")
    lines.append(f"- 생성시각: {now}\n")
    lines.append(f"- CSV 입력: `{CSV_DIR}` (ECC 엑셀 다운로드를 soffice로 CSV 변환)\n")
    lines.append(f"- API: `{API_BASE}`\n")
    lines.append("\n")

    lines.append("## 요약\n")
    lines.append(f"- 비교 대상 과목코드(유니크): **{total_codes}**\n")
    lines.append(f"- API 미존재: **{len(missing_in_api)}**\n")
    lines.append(f"- 수강대상 불일치: **{len(mismatches)}**\n")
    lines.append("\n")

    lines.append("## 파일별 통계\n")
    lines.append("- 형식: 파일 / 유니크코드 / API미존재 / 불일치\n")
    for fname, uniq, miss, mm in sorted(per_file_stats, key=lambda x: (-x[3], -x[2], x[0])):
        lines.append(f"- `{fname}` / {uniq} / {miss} / {mm}\n")
    lines.append("\n")

    if missing_in_api:
        lines.append("## API 미존재 과목 (샘플)\n")
        for fname, code, name in missing_in_api[:50]:
            lines.append(f"- `{fname}` {code} {name}\n")
        if len(missing_in_api) > 50:
            lines.append(f"- ... ({len(missing_in_api)-50} more)\n")
        lines.append("\n")

    if mismatches:
        lines.append("## 수강대상 불일치 (샘플)\n")
        lines.append("- 표기: 파일 / 과목코드 / 과목명 / ECC 수강대상 / API target\n")
        for fname, code, name, ecc_t, api_t in mismatches[:80]:
            lines.append(f"- `{fname}` / {code} / {name} / `{ecc_t}` / `{api_t}`\n")
        if len(mismatches) > 80:
            lines.append(f"- ... ({len(mismatches)-80} more)\n")
        lines.append("\n")

    lines.append("## 해석 가이드\n")
    lines.append("- ECC의 `수강대상` 문자열과 API의 `target`은 공백/개행 차이가 있을 수 있어 정규화 후 비교했습니다(모든 공백을 1칸으로 축약).\n")
    lines.append("- 불일치가 존재하면, (1) ECC 다운로드 데이터가 최신/정본인지, (2) DB 적재 당시 target 파싱/정규화가 달라졌는지 확인이 필요합니다.\n")

    OUT_PATH.write_text("".join(lines), encoding="utf-8")
    print(str(OUT_PATH))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
