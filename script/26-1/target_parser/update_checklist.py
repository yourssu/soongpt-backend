#!/usr/bin/env python3
"""
Update TEST_CHECKLIST.md with review results
"""

import re
from pathlib import Path

CHECKLIST_FILE = Path(__file__).parent / "TEST_CHECKLIST.md"

# Test results: (dept, grade, category, is_appropriate)
RESULTS = [
    # Appropriate tests [x]
    ("국어국문학과", 4, "전필", True),
    ("국어국문학과", 4, "교필", True),
    ("국어국문학과", 2, "채플", True),
    ("차세대반도체학과", 5, "전기", True),
    ("차세대반도체학과", 1, "교필", True),
    ("차세대반도체학과", 3, "교선", True),
    ("물리학과", 4, "전기", True),
    ("물리학과", 1, "교선", True),
    ("수학과", 4, "전필", True),
    ("수학과", 4, "교선", True),
    ("수학과", 5, "채플", True),
    ("영어영문학과", 4, "전선", True),
    ("영어영문학과", 5, "교선", True),
    ("영어영문학과", 1, "교필", True),
    ("철학과", 4, "전선", True),
    ("철학과", 2, "교필", True),
    ("철학과", 1, "교필", True),
    ("화학과", 4, "전선", True),
    ("화학과", 4, "교필", True),
    ("화학과", 5, "교선", True),
    ("정보통계보험수리학과", 1, "전선", True),
    ("정보통계보험수리학과", 5, "교선", True),
    ("정보통계보험수리학과", 4, "전기", True),
    ("일어일문학과", 1, "전선", True),
    ("일어일문학과", 1, "교필", True),
    ("불어불문학과", 3, "전필", True),
    ("독어독문학과", 1, "전기", True),
    ("독어독문학과", 3, "교선", True),
    ("독어독문학과", 3, "기타", True),
    ("의생명시스템학부", 2, "전기", True),
    ("의생명시스템학부", 2, "교필", True),
    ("의생명시스템학부", 4, "전필", True),
    ("기독교학과", 2, "전선", True),
    ("기독교학과", 3, "교선", True),
    ("기독교학과", 1, "교선", True),
    ("중어중문학과", 4, "전선", True),
    ("중어중문학과", 1, "교선", True),
    ("사학과", 2, "교필", True),
    ("사학과", 5, "전필", True),
    ("스포츠학부", 2, "전선", True),
    ("스포츠학부", 2, "교선", True),
    ("스포츠학부", 1, "교선", True),
    ("예술창작학부 문예창작전공", 4, "전필", True),
    ("예술창작학부 영화예술전공", 5, "전기", True),
    ("자유전공학부", 1, "전필", True),
    ("자유전공학부", 3, "교선", True),
    ("자유전공학부", 2, "전필", True),

    # Inappropriate tests [!]
    ("중어중문학과", 1, "전필", False),
    ("국어국문학과", 2, "전필", False),
    ("사학과", 1, "전필", False),
    ("일어일문학과", 1, "전필", False),
    ("일어일문학과", 3, "전필", False),
]

def update_checklist():
    content = CHECKLIST_FILE.read_text(encoding='utf-8')
    lines = content.split('\n')

    current_dept = None

    for i in range(len(lines)):
        line = lines[i]

        # Track department
        if line.startswith('#### '):
            current_dept = line.replace('#### ', '').strip()

        # Parse table rows with checkboxes
        if '|' in line and '**' in line and '학년' in line and current_dept:
            parts = [p.strip() for p in line.split('|')]
            if len(parts) >= 9:
                grade_text = parts[1]
                grade_match = re.search(r'(\d+)학년', grade_text)
                if grade_match:
                    grade = int(grade_match.group(1))

                    # Check if this row matches any of our results
                    categories = ['전필', '전선', '전기', '교필', '교선', '채플', '기타']

                    for cat_idx, cat in enumerate(categories):
                        checkbox_idx = cat_idx + 2

                        # Find matching result
                        for dept, result_grade, result_cat, is_appropriate in RESULTS:
                            if (dept == current_dept and
                                result_grade == grade and
                                result_cat == cat):

                                # Update checkbox
                                if is_appropriate:
                                    parts[checkbox_idx] = ' [x] '
                                else:
                                    parts[checkbox_idx] = ' [!] '

                                print(f"Updated: {dept} - {grade}학년 - {cat} → {'[x]' if is_appropriate else '[!]'}")

                    # Reconstruct line
                    lines[i] = '|'.join(parts)

    # Write back
    CHECKLIST_FILE.write_text('\n'.join(lines), encoding='utf-8')
    print(f"\nChecklist updated: {CHECKLIST_FILE}")

if __name__ == "__main__":
    update_checklist()