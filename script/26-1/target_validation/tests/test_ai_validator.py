from __future__ import annotations

import sys
import unittest
from pathlib import Path

MODULE_DIR = Path(__file__).resolve().parents[1]
if str(MODULE_DIR) not in sys.path:
    sys.path.insert(0, str(MODULE_DIR))

from ai_validator_gemini import GeminiFlashValidator
from schemas import TargetRef


class AiValidatorTest(unittest.TestCase):
    def test_parse_successful_ai_response(self) -> None:
        def fake_request(_: str, __: dict, ___: int) -> dict:
            return {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": (
                                        '{"verdict":"OK","normalized_targets":[{"department":"컴퓨터학부","grade":2}],'
                                        '"unknown_terms":[],"confidence":0.92,"reason":"ok"}'
                                    )
                                }
                            ]
                        }
                    }
                ]
            }

        validator = GeminiFlashValidator(
            api_key="dummy",
            model="gemini-flash-3.0",
            request_fn=fake_request,
        )
        result = validator.validate(
            raw_target_text="컴퓨터학부 2학년",
            parsed_targets=[TargetRef(department="컴퓨터학부", grade=2)],
            unparsed_tokens=[],
            allowed_departments=["컴퓨터학부"],
        )

        self.assertEqual(result.verdict, "OK")
        self.assertEqual(result.confidence, 0.92)
        self.assertEqual(len(result.normalized_targets), 1)
        self.assertEqual(result.normalized_targets[0].department, "컴퓨터학부")
        self.assertEqual(result.normalized_targets[0].grade, 2)

    def test_model_alias_resolution(self) -> None:
        captured: dict[str, str] = {}

        def fake_request(endpoint: str, _: dict, __: int) -> dict:
            captured["endpoint"] = endpoint
            return {
                "candidates": [
                    {
                        "content": {
                            "parts": [
                                {
                                    "text": (
                                        '{"verdict":"OK","normalized_targets":[{"department":"컴퓨터학부","grade":2}],'
                                        '"unknown_terms":[],"confidence":0.91,"reason":"ok"}'
                                    )
                                }
                            ]
                        }
                    }
                ]
            }

        validator = GeminiFlashValidator(
            api_key="dummy",
            model="gemini-flash-3.0",
            request_fn=fake_request,
        )
        validator.validate(
            raw_target_text="컴퓨터학부 2학년",
            parsed_targets=[TargetRef(department="컴퓨터학부", grade=2)],
            unparsed_tokens=[],
            allowed_departments=["컴퓨터학부"],
        )
        self.assertIn("models/gemini-3-flash-preview:generateContent", captured["endpoint"])


if __name__ == "__main__":
    unittest.main()
