#!/usr/bin/env python3
"""Compare ECC downloads (source of truth) '수강대상' vs API targets fetched via /api/courses/by-category.

Why /by-category:
- This endpoint exercises the same retrieval path used by clients: (schoolId, department, grade, [category])
  and internally applies target rules.

Approach:
- For each ECC download CSV (converted from xls), collect unique course codes and ECC target strings.
- For each file, call /api/courses/by-category for grade=1..5 (category omitted => all categories) and union results.
- Build API mapping code -> target (and list of grades it appeared in).
- Compare ECC target vs API target using semantic normalization (ignore formatting noise).

Outputs:
- Markdown report with:
  - per file: download codes, api-retrieved codes, missing, target mismatches, extra(api-only)
  - mismatch samples

Env vars:
- API_BASE (default https://api.dev.soongpt.yourssu.com)
- SCHOOL_ID (default 25)
- CSV_DIR (default script/26-1/downloads_csv)
- OUT_PATH (optional)
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
from typing import Dict, Iterable, List, Optional, Set, Tuple

API_BASE = os.environ.get("API_BASE", "https://api.dev.soongpt.yourssu.com").rstrip("/")
SCHOOL_ID = int(os.environ.get("SCHOOL_ID", "25"))
CSV_DIR = Path(os.environ.get("CSV_DIR", "/home/ubuntu/soongpt-backend/script/26-1/downloads_csv"))
OUT_PATH = Path(
    os.environ.get(
        "OUT_PATH",
        f"/home/ubuntu/soongpt-backend/script/26-1/docs/target_parser/ECC다운로드_vs_API(by-category)_수강대상_대조보고서_{dt.datetime.now().strftime('%Y%m%d_%H%M%S')}.md",
    )
)

CODE_COL = "과목번호"
TARGET_COL = "수강대상"
NAME_COL = "과목명"

_ws_re = re.compile(r"\s+")
_paren_dup_re = re.compile(r"(\([^)]*\))(?:\1)+")


def norm(s: str | None) -> str:
    if s is None:
        return ""
    s = s.replace("\u00a0", " ")
    s = _ws_re.sub(" ", s)
    return s.strip()


def norm_semantic(s: str | None) -> str:
    s = norm(s)
    if not s:
        return ""

    # unify comma spacing
    s = re.sub(r"\s*,\s*", ", ", s)

    # collapse duplicated parenthetical groups
    prev = None
    while prev != s:
        prev = s
        s = _paren_dup_re.sub(r"\1", s)

    # common noisy repeat
    s = re.sub(r"\(대상외수강제한\)\s*\(대상외수강제한\)", "(대상외수강제한)", s)

    return s.strip()


@dataclass
class DlRow:
    code: int
    name: str
    ecc_target: str


def iter_download_rows(csv_path: Path) -> Iterable[DlRow]:
    with csv_path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        need = [CODE_COL, TARGET_COL, NAME_COL]
        missing = [c for c in need if c not in (reader.fieldnames or [])]
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
            yield DlRow(code=code, name=norm(r.get(NAME_COL)), ecc_target=norm(r.get(TARGET_COL)))


def department_from_filename(name: str) -> str:
    # files are like "IT대학_컴퓨터학부.csv" or "IT대학_전자정보공학부 IT융합전공.csv"
    base = name[:-4] if name.endswith(".csv") else name
    if "_" in base:
        return base.split("_", 1)[1].strip()
    return base.strip()


def api_by_category(department: str, grade: int) -> Tuple[Optional[List[Dict]], Optional[int]]:
    """Return (result, http_status_if_error).

    Some departments from ECC downloads may not exist in API's department dictionary.
    In that case dev responds 404; we surface that in the report.
    """
    # NOTE: some department names contain parentheses like "정보보호학과(계약학과)".
    # Use quote (not quote_plus) so parentheses are encoded.
    qs = urllib.parse.urlencode(
        {
            "schoolId": str(SCHOOL_ID),
            "department": department,
            "grade": str(grade),
        },
        doseq=True,
        quote_via=urllib.parse.quote,
        safe="",
    )
    url = f"{API_BASE}/api/courses/by-category?{qs}"
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        return (data.get("result") or [], None)
    except urllib.error.HTTPError as e:
        return (None, int(getattr(e, "code", 0) or 0))


def main() -> int:
    csv_files = sorted([p for p in CSV_DIR.glob("*.csv") if p.is_file()])
    if not csv_files:
        print(f"No csv files in {CSV_DIR}", file=sys.stderr)
        return 2

    now = dt.datetime.now()

    # Per file download map
    dl_maps: Dict[str, Dict[int, DlRow]] = {}
    for p in csv_files:
        m: Dict[int, DlRow] = {}
        for row in iter_download_rows(p):
            prev = m.get(row.code)
            # keep the longest target (often most informative)
            if prev is None or len(row.ecc_target) > len(prev.ecc_target):
                m[row.code] = row
        dl_maps[p.name] = m

    # For each file, hit API per grade and union
    report_per_file = []
    mismatch_rows: List[Tuple[str, int, str, str, str, str]] = []

    for fname, rows in dl_maps.items():
        dept = department_from_filename(fname)

        api_map: Dict[int, Dict] = {}
        seen_grades: Dict[int, Set[int]] = {}
        api_error: Optional[int] = None
        for g in range(1, 6):
            items, err = api_by_category(dept, g)
            if err is not None:
                api_error = err
                break
            assert items is not None
            for it in items:
                try:
                    code = int(it.get("code"))
                except Exception:
                    continue
                api_map[code] = it
                seen_grades.setdefault(code, set()).add(g)

        dl_codes = set(rows.keys())
        api_codes = set(api_map.keys())

        missing = sorted(dl_codes - api_codes)
        extra = sorted(api_codes - dl_codes)
        missing_details = [(c, rows[c].name, rows[c].ecc_target) for c in missing]

        mismatches = 0
        for code, dl_row in rows.items():
            api_item = api_map.get(code)
            if api_item is None:
                continue
            ecc_n = norm_semantic(dl_row.ecc_target)
            api_t = norm(api_item.get("target"))
            api_n = norm_semantic(api_t)

            # treat truncated prefix as equal (download CSV conversion may truncate)
            short, long = (ecc_n, api_n) if len(ecc_n) <= len(api_n) else (api_n, ecc_n)
            is_trunc_prefix = len(short) >= 30 and long.startswith(short)

            if ecc_n != api_n and not is_trunc_prefix:
                mismatches += 1
                mismatch_rows.append(
                    (
                        fname,
                        code,
                        dl_row.name,
                        dl_row.ecc_target,
                        api_t,
                        ",".join(map(str, sorted(seen_grades.get(code, set())))),
                    )
                )

        report_per_file.append(
            {
                "file": fname,
                "department": dept,
                "download_codes": len(dl_codes),
                "api_codes": len(api_codes),
                "missing": len(missing),
                "mismatches": mismatches,
                "extra": len(extra),
                "api_error": api_error,
                "missing_details": missing_details,
            }
        )

    # overall summary
    total_dl = sum(x["download_codes"] for x in report_per_file)
    total_missing = sum(x["missing"] for x in report_per_file)
    total_mismatch = sum(x["mismatches"] for x in report_per_file)
    total_extra = sum(x["extra"] for x in report_per_file)
    total_api_errors = sum(1 for x in report_per_file if x.get("api_error"))

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)

    lines: List[str] = []
    lines.append("# ECC 다운로드 vs API(by-category) 수강대상(target) 대조 보고서\n")
    lines.append(f"- 생성시각: {now.strftime('%Y-%m-%d %H:%M:%S')}\n")
    lines.append(f"- Downloads(원본): `{CSV_DIR}` (ECC xls를 soffice로 csv 변환)\n")
    lines.append(f"- API: `{API_BASE}`\n")
    lines.append(f"- 사용 엔드포인트: `GET /api/courses/by-category?schoolId={SCHOOL_ID}&department=...&grade=1..5` (category 미지정=전체)\n")
    lines.append("\n")

    lines.append("## 요약\n")
    lines.append(f"- 파일 수: **{len(report_per_file)}**\n")
    lines.append(f"- API 4xx/5xx 조회 실패(학과 단위): **{total_api_errors}**\n")
    lines.append(f"- Downloads 유니크 코드 합계(파일별 유니크 합): **{total_dl}**\n")
    lines.append(f"- API 조회 결과에 없는 코드(Downloads 기준 누락): **{total_missing}**\n")
    lines.append(f"- 의미적 수강대상 불일치: **{total_mismatch}**\n")
    lines.append(
        f"- API에만 존재(Downloads에 없음): **{total_extra}** (참고: `/by-category`는 학과+학년 기준 ‘조회 가능한 전체 강의’를 반환하므로 Downloads 범위와 다를 수 있음)\n"
    )
    lines.append("\n")

    lines.append("## 파일별 통계\n")
    lines.append("- 형식: 파일 / 학과 / API에러 / DL코드 / API코드 / DL누락(API미포함) / 수강대상 불일치 / API추가\n")
    for x in sorted(report_per_file, key=lambda r: (-(r.get("api_error") or 0), -r["missing"], -r["mismatches"], r["file"])):
        err = x.get("api_error")
        err_s = str(err) if err else "-"
        lines.append(
            f"- `{x['file']}` / {x['department']} / {err_s} / {x['download_codes']} / {x['api_codes']} / {x['missing']} / {x['mismatches']} / {x['extra']}\n"
        )
    lines.append("\n")

    # Missing details
    missing_any = [x for x in report_per_file if x.get("missing")]
    if missing_any:
        lines.append("## Downloads에만 존재(= API by-category로 조회되지 않음)\n")
        lines.append("- Downloads가 원본이므로, 아래 과목은 **DB의 target/조회 로직 관점에서 해당 학과에서 조회가 안 되는 상태**일 가능성이 큼\n")
        for x in sorted(missing_any, key=lambda r: (-r.get("api_error") if r.get("api_error") else 0, -r["missing"], r["file"])):
            if x.get("api_error"):
                lines.append(f"- `{x['file']}` ({x['department']}): API error {x['api_error']} → 학과 파라미터가 API에서 미지원/미매핑 가능\n")
                continue
            lines.append(f"- `{x['file']}` ({x['department']}): missing {x['missing']}\n")
            for code, name, ecc_t in (x.get("missing_details") or [])[:20]:
                lines.append(f"  - {code} {name} / `{ecc_t}`\n")
        lines.append("\n")

    if mismatch_rows:
        lines.append("## 수강대상 불일치(의미적)\n")
        lines.append("- 표기: 파일 / 과목코드 / 과목명 / ECC 수강대상 / API target / API에서 등장한 grade\n")
        for (fname, code, name, ecc_t, api_t, grades) in mismatch_rows[:120]:
            lines.append(f"- `{fname}` / {code} / {name} / `{ecc_t}` / `{api_t}` / {grades}\n")
        if len(mismatch_rows) > 120:
            lines.append(f"- ... ({len(mismatch_rows)-120} more)\n")
        lines.append("\n")

    lines.append("## 비교 기준(의미적 동일성)\n")
    lines.append("- 공백/개행/쉼표 간격 등 포맷 차이는 무시\n")
    lines.append("- `(대상외수강제한)` 등 괄호 문구가 중복된 경우 1회로 축약\n")
    lines.append("- CSV 변환 과정에서 긴 셀 문자열이 잘린 경우가 있어, 한쪽이 다른 쪽의 충분히 긴 prefix이면 동일로 간주\n")
    lines.append("\n")

    lines.append("## 해석\n")
    lines.append("- Downloads가 원본이므로, `DL누락(API미포함)` 또는 `수강대상 불일치`가 1건이라도 존재하면 DB target 또는 조회 로직(grade/department 적용)에 문제가 있을 가능성이 큼.\n")

    OUT_PATH.write_text("".join(lines), encoding="utf-8")
    print(str(OUT_PATH))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
