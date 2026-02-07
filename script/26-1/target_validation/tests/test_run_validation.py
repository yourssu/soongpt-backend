from __future__ import annotations

import csv
import json
import sys
import tempfile
import unittest
from pathlib import Path

MODULE_DIR = Path(__file__).resolve().parents[1]
if str(MODULE_DIR) not in sys.path:
    sys.path.insert(0, str(MODULE_DIR))

from run_validation import run_validation


class RunValidationTest(unittest.TestCase):
    def test_pipeline_blocks_invalid_rows(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            csv_path = temp_path / "input.csv"
            data_yml_path = temp_path / "data.yml"
            alias_yml_path = temp_path / "alias.yml"
            report_path = temp_path / "report.json"
            manual_path = temp_path / "manual.csv"
            env_path = temp_path / ".env"

            self._write_csv(
                csv_path,
                [
                    {"code": "C101", "target": "전체학년 소프트"},
                    {"code": "C102", "target": "없는학과 2학년"},
                    {"code": "C103", "target": "컴퓨터학부 6학년"},
                ],
            )
            data_yml_path.write_text(
                "ssu-data:\n"
                "  colleges:\n"
                "    - name: IT대학\n"
                "      departments:\n"
                "        - 소프트웨어학부\n"
                "        - 컴퓨터학부\n",
                encoding="utf-8",
            )
            alias_yml_path.write_text(
                "aliases:\n"
                "  소프트: 소프트웨어학부\n",
                encoding="utf-8",
            )
            env_path.write_text("gemini_api_key=\n", encoding="utf-8")

            report, exit_code = run_validation(
                csv_path=str(csv_path),
                data_yml_path=str(data_yml_path),
                alias_yml_path=str(alias_yml_path),
                report_path=str(report_path),
                manual_review_path=str(manual_path),
                env_path=str(env_path),
                gemini_model="gemini-flash-3.0",
                disable_ai=True,
            )

            self.assertEqual(exit_code, 2)
            self.assertEqual(report.summary.total_rows, 3)
            self.assertGreaterEqual(report.summary.blocked_rows, 2)
            self.assertTrue(report_path.exists())
            self.assertTrue(manual_path.exists())

            payload = json.loads(report_path.read_text(encoding="utf-8"))
            self.assertIn("summary", payload)
            self.assertIn("rows", payload)

    def test_pipeline_passes_when_all_rows_valid(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            csv_path = temp_path / "input.csv"
            data_yml_path = temp_path / "data.yml"
            alias_yml_path = temp_path / "alias.yml"
            report_path = temp_path / "report.json"
            manual_path = temp_path / "manual.csv"
            env_path = temp_path / ".env"

            self._write_csv(
                csv_path,
                [
                    {"code": "C201", "target": "컴퓨터학부 2학년"},
                    {"code": "C202", "target": "AI융합학부 1~2학년"},
                ],
            )
            data_yml_path.write_text(
                "ssu-data:\n"
                "  colleges:\n"
                "    - name: IT대학\n"
                "      departments:\n"
                "        - 컴퓨터학부\n"
                "        - AI융합학부\n",
                encoding="utf-8",
            )
            alias_yml_path.write_text("aliases: {}\n", encoding="utf-8")
            env_path.write_text("gemini_api_key=\n", encoding="utf-8")

            report, exit_code = run_validation(
                csv_path=str(csv_path),
                data_yml_path=str(data_yml_path),
                alias_yml_path=str(alias_yml_path),
                report_path=str(report_path),
                manual_review_path=str(manual_path),
                env_path=str(env_path),
                gemini_model="gemini-flash-3.0",
                disable_ai=True,
            )

            self.assertEqual(exit_code, 0)
            self.assertEqual(report.summary.total_rows, 2)
            self.assertEqual(report.summary.blocked_rows, 0)
            self.assertEqual(report.summary.pass_rows, 2)

    def test_fail_on_ai_skipped(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            csv_path = temp_path / "input.csv"
            data_yml_path = temp_path / "data.yml"
            alias_yml_path = temp_path / "alias.yml"
            report_path = temp_path / "report.json"
            manual_path = temp_path / "manual.csv"
            env_path = temp_path / ".env"

            self._write_csv(csv_path, [{"code": "C301", "target": "컴퓨터학부 2학년"}])
            data_yml_path.write_text(
                "ssu-data:\n"
                "  colleges:\n"
                "    - name: IT대학\n"
                "      departments:\n"
                "        - 컴퓨터학부\n",
                encoding="utf-8",
            )
            alias_yml_path.write_text("aliases: {}\n", encoding="utf-8")
            env_path.write_text("gemini_api_key=\n", encoding="utf-8")

            report, exit_code = run_validation(
                csv_path=str(csv_path),
                data_yml_path=str(data_yml_path),
                alias_yml_path=str(alias_yml_path),
                report_path=str(report_path),
                manual_review_path=str(manual_path),
                env_path=str(env_path),
                gemini_model="gemini-flash-3.0",
                fail_on_ai_skipped=True,
            )

            self.assertEqual(exit_code, 4)
            self.assertGreater(report.summary.ai_skipped_rows, 0)
            self.assertIn("ai_skipped_rows>0", report.summary.gate_failures)

    def test_require_ai_invoked(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            csv_path = temp_path / "input.csv"
            data_yml_path = temp_path / "data.yml"
            alias_yml_path = temp_path / "alias.yml"
            report_path = temp_path / "report.json"
            manual_path = temp_path / "manual.csv"
            env_path = temp_path / ".env"

            self._write_csv(csv_path, [{"code": "C302", "target": "컴퓨터학부 2학년"}])
            data_yml_path.write_text(
                "ssu-data:\n"
                "  colleges:\n"
                "    - name: IT대학\n"
                "      departments:\n"
                "        - 컴퓨터학부\n",
                encoding="utf-8",
            )
            alias_yml_path.write_text("aliases: {}\n", encoding="utf-8")
            env_path.write_text("gemini_api_key=\n", encoding="utf-8")

            report, exit_code = run_validation(
                csv_path=str(csv_path),
                data_yml_path=str(data_yml_path),
                alias_yml_path=str(alias_yml_path),
                report_path=str(report_path),
                manual_review_path=str(manual_path),
                env_path=str(env_path),
                gemini_model="gemini-flash-3.0",
                require_ai_invoked=True,
            )

            self.assertEqual(exit_code, 5)
            self.assertEqual(report.summary.ai_invoked_rows, 0)
            self.assertIn("ai_invoked_rows<=0", report.summary.gate_failures)

    def test_min_coverage_rate_gate(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            csv_path = temp_path / "input.csv"
            data_yml_path = temp_path / "data.yml"
            alias_yml_path = temp_path / "alias.yml"
            report_path = temp_path / "report.json"
            manual_path = temp_path / "manual.csv"
            env_path = temp_path / ".env"

            with open(csv_path, "w", encoding="utf-8", newline="") as file:
                writer = csv.DictWriter(file, fieldnames=["code", "target"])
                writer.writeheader()
            data_yml_path.write_text(
                "ssu-data:\n"
                "  colleges:\n"
                "    - name: IT대학\n"
                "      departments:\n"
                "        - 컴퓨터학부\n",
                encoding="utf-8",
            )
            alias_yml_path.write_text("aliases: {}\n", encoding="utf-8")
            env_path.write_text("gemini_api_key=\n", encoding="utf-8")

            report, exit_code = run_validation(
                csv_path=str(csv_path),
                data_yml_path=str(data_yml_path),
                alias_yml_path=str(alias_yml_path),
                report_path=str(report_path),
                manual_review_path=str(manual_path),
                env_path=str(env_path),
                gemini_model="gemini-flash-3.0",
                disable_ai=True,
                min_coverage_rate=90,
            )

            self.assertEqual(exit_code, 6)
            self.assertIn("coverage_rate<90", report.summary.gate_failures)

    @staticmethod
    def _write_csv(path: Path, rows: list[dict[str, str]]) -> None:
        with open(path, "w", encoding="utf-8", newline="") as file:
            writer = csv.DictWriter(file, fieldnames=["code", "target"])
            writer.writeheader()
            writer.writerows(rows)


if __name__ == "__main__":
    unittest.main()
