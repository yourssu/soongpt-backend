from __future__ import annotations

import sys
import unittest
from pathlib import Path

MODULE_DIR = Path(__file__).resolve().parents[1]
if str(MODULE_DIR) not in sys.path:
    sys.path.insert(0, str(MODULE_DIR))

from parser_adapter import build_alias_lookup, parse_target_text


class ParserAdapterTest(unittest.TestCase):
    def setUp(self) -> None:
        allowed = ["소프트웨어학부", "컴퓨터학부", "AI융합학부"]
        aliases = {"소프트": "소프트웨어학부", "AI": "AI융합학부"}
        self.alias_lookup = build_alias_lookup(allowed, aliases)

    def test_parse_whole_grade_alias(self) -> None:
        parsed, unparsed = parse_target_text("전체학년 소프트", self.alias_lookup)
        self.assertEqual(len(parsed), 5)
        self.assertTrue(all(target.department == "소프트웨어학부" for target in parsed))
        self.assertEqual({target.grade for target in parsed}, {1, 2, 3, 4, 5})
        self.assertEqual(unparsed, [])

    def test_parse_grade_range(self) -> None:
        parsed, unparsed = parse_target_text("컴퓨터학부 1~3학년", self.alias_lookup)
        self.assertEqual({target.department for target in parsed}, {"컴퓨터학부"})
        self.assertEqual({target.grade for target in parsed}, {1, 2, 3})
        self.assertEqual(unparsed, [])

    def test_unparsed_when_department_unknown(self) -> None:
        parsed, unparsed = parse_target_text("없는학과 2학년", self.alias_lookup)
        self.assertEqual(parsed, [])
        self.assertTrue(unparsed)

    def test_parse_whole_target_with_allowed_departments(self) -> None:
        allowed = ["소프트웨어학부", "컴퓨터학부", "AI융합학부"]
        parsed, unparsed = parse_target_text("전체", self.alias_lookup, allowed_departments=allowed)
        self.assertEqual(len(parsed), 15)
        self.assertEqual({target.department for target in parsed}, set(allowed))
        self.assertEqual(unparsed, [])


if __name__ == "__main__":
    unittest.main()
