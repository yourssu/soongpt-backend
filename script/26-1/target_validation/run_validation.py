from __future__ import annotations

import argparse
import csv
import json
import sys
from pathlib import Path

from ai_validator_gemini import GeminiFlashValidator
from decision_engine import DecisionEngine
from env_loader import load_api_key
from parser_adapter import build_alias_lookup, parse_target_text, read_input_rows
from rule_validator import RuleValidator
from schemas import AiValidationResult, RowValidationResult, ValidationReport, ValidationSummary
from yml_loader import load_aliases, load_departments


def run_validation(
    csv_path: str,
    data_yml_path: str,
    alias_yml_path: str,
    report_path: str,
    manual_review_path: str,
    env_path: str,
    gemini_model: str,
    target_column: str | None = None,
    course_code_column: str | None = None,
    disable_ai: bool = False,
    ai_confidence_threshold: float = 0.8,
    fail_on_manual_review: bool = False,
    fail_on_ai_skipped: bool = False,
    require_ai_invoked: bool = False,
    min_coverage_rate: float | None = None,
    prompt_path: str | None = None,
) -> tuple[ValidationReport, int]:
    allowed_departments = load_departments(data_yml_path)
    aliases = load_aliases(alias_yml_path)
    alias_lookup = build_alias_lookup(allowed_departments, aliases)
    rows, _, _ = read_input_rows(
        csv_path=csv_path,
        target_column=target_column,
        course_code_column=course_code_column,
    )

    rule_validator = RuleValidator(allowed_departments=allowed_departments)
    decision_engine = DecisionEngine(ai_confidence_threshold=ai_confidence_threshold)

    gemini_api_key = "" if disable_ai else load_api_key(env_path, key_name="gemini_api_key")
    ai_validator = None
    if not disable_ai:
        if not gemini_api_key:
            print(
                "[WARN] Gemini API key(gemini_api_key)가 비어 있어 AI 검증이 모두 SKIPPED 처리됩니다. "
                f"env_path={env_path}",
                file=sys.stderr,
            )
        ai_validator = GeminiFlashValidator(
            api_key=gemini_api_key,
            model=gemini_model,
            prompt_path=prompt_path,
        )
    ai_cache: dict[tuple[str, tuple[tuple[str, int], ...], tuple[str, ...]], AiValidationResult] = {}

    summary = ValidationSummary(total_rows=len(rows))
    row_results: list[RowValidationResult] = []

    for row in rows:
        parsed_targets, unparsed_tokens = parse_target_text(
            row.raw_target_text,
            alias_lookup,
            allowed_departments=allowed_departments,
        )
        issues = rule_validator.validate(
            row_no=row.row_no,
            course_code=row.course_code,
            raw_target_text=row.raw_target_text,
            parsed_targets=parsed_targets,
            unparsed_tokens=unparsed_tokens,
        )

        has_blocking_issue = any(issue.severity == "ERROR" for issue in issues)
        ai_result = None
        if ai_validator:
            if has_blocking_issue:
                ai_result = None
            else:
                cache_key = (
                    row.raw_target_text,
                    tuple((target.department, target.grade) for target in parsed_targets),
                    tuple(unparsed_tokens),
                )
                cached = ai_cache.get(cache_key)
                if cached is not None:
                    ai_result = cached
                    summary.ai_cache_hits += 1
                else:
                    ai_result = ai_validator.validate(
                        raw_target_text=row.raw_target_text,
                        parsed_targets=parsed_targets,
                        unparsed_tokens=unparsed_tokens,
                        allowed_departments=allowed_departments,
                    )
                    ai_cache[cache_key] = ai_result
                    summary.ai_api_calls += 1
                if ai_result.verdict == "SKIPPED":
                    summary.ai_skipped_rows += 1
                else:
                    summary.ai_invoked_rows += 1

        status, status_reason, final_targets = decision_engine.decide(
            parsed_targets=parsed_targets,
            issues=issues,
            ai_result=ai_result,
        )

        if status == "BLOCKED":
            summary.blocked_rows += 1
        elif status == "MANUAL_REVIEW":
            summary.manual_review_rows += 1
        else:
            summary.pass_rows += 1

        unknown_count = len([issue for issue in issues if issue.code == "UNKNOWN_DEPARTMENT"])
        unparsed_count = len([issue for issue in issues if issue.code == "UNPARSED_TOKENS"])
        summary.unknown_department_count += unknown_count
        summary.unparsed_token_rows += unparsed_count
        summary.blocking_errors += len([issue for issue in issues if issue.severity == "ERROR"])

        row_results.append(
            RowValidationResult(
                row_no=row.row_no,
                course_code=row.course_code,
                raw_target_text=row.raw_target_text,
                parsed_targets=parsed_targets,
                unparsed_tokens=unparsed_tokens,
                issues=issues,
                ai_result=ai_result,
                status=status,
                status_reason=status_reason,
                final_targets=final_targets,
            )
        )

    parsed_ok_rows = len([item for item in row_results if not item.unparsed_tokens and item.parsed_targets])
    summary.coverage_rate = round((parsed_ok_rows / summary.total_rows) * 100, 2) if summary.total_rows else 0.0

    report = ValidationReport(summary=summary, rows=row_results)

    exit_code = 0
    if summary.blocked_rows > 0:
        exit_code = 2
        summary.gate_failures.append("blocked_rows>0")
    elif fail_on_manual_review and summary.manual_review_rows > 0:
        exit_code = 3
        summary.gate_failures.append("manual_review_rows>0")
    elif fail_on_ai_skipped and summary.ai_skipped_rows > 0:
        exit_code = 4
        summary.gate_failures.append("ai_skipped_rows>0")
    elif require_ai_invoked and summary.ai_invoked_rows <= 0:
        exit_code = 5
        summary.gate_failures.append("ai_invoked_rows<=0")
    elif min_coverage_rate is not None and summary.coverage_rate < min_coverage_rate:
        exit_code = 6
        summary.gate_failures.append(f"coverage_rate<{min_coverage_rate}")

    _write_report(report, report_path)
    _write_manual_review_csv(row_results, manual_review_path)

    return report, exit_code


def _write_report(report: ValidationReport, report_path: str) -> None:
    output = Path(report_path)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(
        json.dumps(report.to_dict(), ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def _write_manual_review_csv(rows: list[RowValidationResult], manual_review_path: str) -> None:
    output = Path(manual_review_path)
    output.parent.mkdir(parents=True, exist_ok=True)
    fields = [
        "row_no",
        "course_code",
        "status",
        "status_reason",
        "raw_target_text",
        "parsed_targets",
        "unparsed_tokens",
        "issues",
        "ai_reason",
    ]
    with open(output, "w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fields)
        writer.writeheader()
        for row in rows:
            if row.status not in {"MANUAL_REVIEW", "BLOCKED"}:
                continue
            writer.writerow(
                {
                    "row_no": row.row_no,
                    "course_code": row.course_code,
                    "status": row.status,
                    "status_reason": row.status_reason,
                    "raw_target_text": row.raw_target_text,
                    "parsed_targets": "|".join(f"{target.department}{target.grade}" for target in row.parsed_targets),
                    "unparsed_tokens": "|".join(row.unparsed_tokens),
                    "issues": "|".join(f"{issue.code}:{issue.message}" for issue in row.issues),
                    "ai_reason": row.ai_result.reason if row.ai_result else "",
                }
            )


def build_parser() -> argparse.ArgumentParser:
    base_dir = Path(__file__).resolve().parent
    parser = argparse.ArgumentParser(description="수강대상 검증 파이프라인")
    parser.add_argument("--csv-path", required=True, help="입력 CSV 경로")
    parser.add_argument("--data-yml-path", default="src/main/resources/data.yml", help="학과 화이트리스트 data.yml 경로")
    parser.add_argument(
        "--alias-yml-path",
        default=str(base_dir / "config" / "department_alias.yml"),
        help="학과 별칭 매핑 yml 경로",
    )
    parser.add_argument(
        "--report-path",
        default=str(base_dir / "reports" / "validation_report.json"),
        help="검증 리포트 출력 경로",
    )
    parser.add_argument(
        "--manual-review-path",
        default=str(base_dir / "reports" / "manual_review.csv"),
        help="수동 검토 CSV 출력 경로",
    )
    parser.add_argument("--env-path", default=".env", help="Gemini API 키가 들어있는 .env 경로")
    parser.add_argument("--gemini-model", default="gemini-flash-3.0", help="Gemini 모델명")
    parser.add_argument("--target-column", default=None, help="수강대상 컬럼명 (명시 시 자동탐지 생략)")
    parser.add_argument("--course-code-column", default=None, help="과목코드 컬럼명 (명시 시 자동탐지 생략)")
    parser.add_argument("--disable-ai", action="store_true", help="AI 검증 비활성화")
    parser.add_argument("--ai-confidence-threshold", type=float, default=0.8, help="AI 자동보정 최소 신뢰도")
    parser.add_argument(
        "--fail-on-manual-review",
        action="store_true",
        help="수동 검토가 하나라도 있으면 non-zero 종료",
    )
    parser.add_argument(
        "--fail-on-ai-skipped",
        action="store_true",
        help="AI 호출 스킵 건이 하나라도 있으면 non-zero 종료",
    )
    parser.add_argument(
        "--require-ai-invoked",
        action="store_true",
        help="AI 호출 성공 건(ai_invoked_rows)이 1건 이상이어야 통과",
    )
    parser.add_argument(
        "--min-coverage-rate",
        type=float,
        default=None,
        help="최소 파싱 커버리지(%%)를 강제",
    )
    parser.add_argument(
        "--prompt-path",
        default=str(base_dir / "prompts" / "target_verify_system.txt"),
        help="Gemini 시스템 프롬프트 파일 경로",
    )
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()

    _, exit_code = run_validation(
        csv_path=args.csv_path,
        data_yml_path=args.data_yml_path,
        alias_yml_path=args.alias_yml_path,
        report_path=args.report_path,
        manual_review_path=args.manual_review_path,
        env_path=args.env_path,
        gemini_model=args.gemini_model,
        target_column=args.target_column,
        course_code_column=args.course_code_column,
        disable_ai=args.disable_ai,
        ai_confidence_threshold=args.ai_confidence_threshold,
        fail_on_manual_review=args.fail_on_manual_review,
        fail_on_ai_skipped=args.fail_on_ai_skipped,
        require_ai_invoked=args.require_ai_invoked,
        min_coverage_rate=args.min_coverage_rate,
        prompt_path=args.prompt_path,
    )
    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
