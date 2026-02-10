#!/usr/bin/env python3
import argparse
import csv
import json
import os
import re
import subprocess
from collections import defaultdict
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Set, Tuple
from urllib.parse import urlencode

import mysql.connector
import requests
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BASE_DIR.parent.parent.parent
DOC_DIR = PROJECT_ROOT / "script" / "26-1" / "docs" / "target_parser"

CATEGORY_KOR_TO_ENUM = {
    "전필": "MAJOR_REQUIRED",
    "전기": "MAJOR_BASIC",
    "전선": "MAJOR_ELECTIVE",
}
CATEGORY_ENUM_TO_KOR = {v: k for k, v in CATEGORY_KOR_TO_ENUM.items()}


def parse_db_config() -> Dict[str, object]:
    env_path = PROJECT_ROOT / ".env"
    if env_path.exists():
        load_dotenv(env_path)

    db_url = os.getenv("DB_URL", "")
    if db_url:
        m = re.match(r"jdbc:mysql://([^:]+):(\d+)/(.+)", db_url)
        if not m:
            raise ValueError(f"Invalid DB_URL format: {db_url}")
        host, port, database = m.group(1), int(m.group(2)), m.group(3)
    else:
        host = os.getenv("DB_HOST", "localhost")
        port = int(os.getenv("DB_PORT", "3306"))
        database = os.getenv("DB_NAME", "soon")

    user = os.getenv("DB_USERNAME", os.getenv("DB_USER", "root"))
    password = os.getenv("DB_PASSWORD", "")

    return {
        "host": host,
        "port": port,
        "database": database,
        "user": user,
        "password": password,
    }


def get_departments(conn) -> List[str]:
    q = """
    SELECT DISTINCT d.name
    FROM course c
    JOIN department d ON d.name = c.department
    WHERE c.category IN ('MAJOR_REQUIRED','MAJOR_BASIC','MAJOR_ELECTIVE')
    ORDER BY d.name
    """
    cur = conn.cursor()
    cur.execute(q)
    items = [r[0] for r in cur.fetchall()]
    cur.close()
    return items


def expected_codes(conn, dept: str, grade: int, category_enum: str) -> Set[int]:
    cur = conn.cursor(dictionary=True)

    cur.execute("SELECT id, college_id FROM department WHERE name=%s", (dept,))
    row = cur.fetchone()
    if not row:
        cur.close()
        return set()

    dept_id = row["id"]
    college_id = row["college_id"]
    grade_col = f"grade{grade}"

    q = f"""
    SELECT DISTINCT c.code
    FROM course c
    WHERE c.category = %s
      AND EXISTS (
        SELECT 1 FROM target t_allow
        WHERE t_allow.course_code = c.code
          AND t_allow.student_type = 0
          AND t_allow.is_denied = 0
          AND t_allow.{grade_col} = 1
          AND (
            t_allow.scope_type = 0
            OR (t_allow.scope_type = 1 AND t_allow.college_id = %s)
            OR (t_allow.scope_type = 2 AND t_allow.department_id = %s)
          )
      )
      AND NOT EXISTS (
        SELECT 1 FROM target t_deny
        WHERE t_deny.course_code = c.code
          AND t_deny.student_type = 0
          AND t_deny.is_denied = 1
          AND t_deny.{grade_col} = 1
          AND (
            t_deny.scope_type = 0
            OR (t_deny.scope_type = 1 AND t_deny.college_id = %s)
            OR (t_deny.scope_type = 2 AND t_deny.department_id = %s)
          )
      )
    """

    cur.execute(q, (category_enum, college_id, dept_id, college_id, dept_id))
    result = {int(r["code"]) for r in cur.fetchall()}
    cur.close()
    return result


def fetch_api_codes(base_url: str, school_id: int, dept: str, grade: int, category_enum: str, use_curl: bool) -> Tuple[Set[int], str, int]:
    endpoint = f"{base_url.rstrip('/')}/api/courses/by-category"
    params = {
        "schoolId": school_id,
        "department": dept,
        "grade": grade,
        "category": category_enum,
    }

    if use_curl:
        url = endpoint + "?" + urlencode(params)
        proc = subprocess.run(
            ["curl", "-sS", "--connect-timeout", "5", "--max-time", "20", "-w", "\n%{http_code}", url],
            capture_output=True,
            text=True,
        )
        out = proc.stdout
        if not out:
            return set(), "EMPTY_RESPONSE", 0
        *body_lines, code_line = out.splitlines()
        body = "\n".join(body_lines)
        try:
            status = int(code_line.strip())
        except ValueError:
            status = 0
        if status != 200:
            return set(), f"HTTP_{status}", status
        try:
            data = json.loads(body)
        except json.JSONDecodeError:
            return set(), "INVALID_JSON", status
    else:
        r = requests.get(endpoint, params=params, timeout=30)
        status = r.status_code
        if status != 200:
            return set(), f"HTTP_{status}", status
        try:
            data = r.json()
        except Exception:
            return set(), "INVALID_JSON", status

    result = data.get("result", [])
    codes = set()
    for item in result:
        code = item.get("code")
        if isinstance(code, int):
            codes.add(code)
        elif isinstance(code, str) and code.isdigit():
            codes.add(int(code))
    return codes, "OK", status


def main() -> None:
    parser = argparse.ArgumentParser(description="Validate major category API (전필/전기/전선) by dept+grade")
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--school-id", type=int, default=25)
    parser.add_argument("--use-curl", action="store_true")
    args = parser.parse_args()

    db_conf = parse_db_config()
    conn = mysql.connector.connect(**db_conf)

    departments = get_departments(conn)
    grades = [1, 2, 3, 4, 5]
    cats = ["MAJOR_REQUIRED", "MAJOR_BASIC", "MAJOR_ELECTIVE"]

    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    out_csv = DOC_DIR / f"전필전기전선_API대조_{ts}.csv"
    out_md = DOC_DIR / f"전필전기전선_API대조_보고서_{ts}.md"

    rows = []
    status_count = defaultdict(int)
    mismatch_count = 0

    total = len(departments) * len(grades) * len(cats)
    done = 0

    for dept in departments:
        for grade in grades:
            for cat in cats:
                done += 1
                exp = expected_codes(conn, dept, grade, cat)
                act, api_status, http_code = fetch_api_codes(
                    args.base_url, args.school_id, dept, grade, cat, args.use_curl
                )

                missing = sorted(exp - act)
                extra = sorted(act - exp)

                if api_status != "OK":
                    status = api_status
                elif not missing and not extra:
                    status = "PASS"
                else:
                    status = "MISMATCH"
                    mismatch_count += 1

                status_count[status] += 1

                rows.append({
                    "department": dept,
                    "grade": grade,
                    "category": cat,
                    "category_kor": CATEGORY_ENUM_TO_KOR.get(cat, cat),
                    "expected_count": len(exp),
                    "actual_count": len(act),
                    "missing_count": len(missing),
                    "extra_count": len(extra),
                    "missing_codes": ",".join(map(str, missing[:200])),
                    "extra_codes": ",".join(map(str, extra[:200])),
                    "status": status,
                    "api_status": api_status,
                    "http_code": http_code,
                })

                if done % 50 == 0 or done == total:
                    print(f"progress {done}/{total}")

    conn.close()

    out_csv.parent.mkdir(parents=True, exist_ok=True)
    with out_csv.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)

    lines = []
    lines.append("# 전필/전기/전선 API 대조 검증 보고서")
    lines.append("")
    lines.append(f"> 실행시각: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    lines.append(f"> baseUrl: `{args.base_url}`")
    lines.append(f"> schoolId: `{args.school_id}`")
    lines.append(f"> 요청 방식: `{'curl' if args.use_curl else 'requests'}`")
    lines.append("")
    lines.append("## 요약")
    lines.append(f"- 총 조합: **{len(rows)}**")
    for k in sorted(status_count.keys()):
        lines.append(f"- {k}: **{status_count[k]}**")
    lines.append(f"- MISMATCH 조합 수: **{mismatch_count}**")
    lines.append("")
    lines.append(f"- 상세 CSV: `{out_csv}`")
    lines.append("")

    mismatch_rows = [r for r in rows if r["status"] == "MISMATCH"]
    error_rows = [r for r in rows if r["status"].startswith("HTTP_") or r["status"] in ("INVALID_JSON", "EMPTY_RESPONSE")]

    if error_rows:
        lines.append("## API 오류 조합 (최대 30건)")
        lines.append("| department | grade | category | status | http_code |")
        lines.append("|---|---:|---|---|---:|")
        for r in error_rows[:30]:
            lines.append(f"| {r['department']} | {r['grade']} | {r['category_kor']} | {r['status']} | {r['http_code']} |")
        lines.append("")

    if mismatch_rows:
        lines.append("## 불일치 샘플 (최대 50건)")
        lines.append("| department | grade | category | expected | actual | missing | extra |")
        lines.append("|---|---:|---|---:|---:|---:|---:|")
        for r in mismatch_rows[:50]:
            lines.append(
                f"| {r['department']} | {r['grade']} | {r['category_kor']} | {r['expected_count']} | {r['actual_count']} | {r['missing_count']} | {r['extra_count']} |"
            )
        lines.append("")

    if not mismatch_rows and not error_rows:
        lines.append("✅ 전 조합 PASS")

    out_md.write_text("\n".join(lines), encoding="utf-8")

    print(f"CSV: {out_csv}")
    print(f"REPORT: {out_md}")


if __name__ == "__main__":
    main()
