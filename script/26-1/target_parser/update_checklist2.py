#!/usr/bin/env python3
"""
Update TEST_CHECKLIST.md with 2nd round review results
"""

import re
from pathlib import Path

CHECKLIST_FILE = Path(__file__).parent / "TEST_CHECKLIST.md"

# 2nd Round Test results
RESULTS = [
    # Appropriate tests [x]
    ("AI소프트웨어학부", 1, "전필", True),  # 0개지만 전선 있음
    ("AI소프트웨어학부", 1, "교필", True),
    ("AI소프트웨어학부", 2, "기타", True),
    ("정보보호학과", 3, "교필", True),
    ("AI융합학부", 2, "전필", True),  # 0개지만 전선 34개 있음
    ("AI융합학부", 4, "전필", True),
    ("AI융합학부", 5, "교선", True),
    ("글로벌미디어학부", 1, "전필", True),
    ("글로벌미디어학부", 2, "교필", True),
    ("글로벌미디어학부", 4, "전선", True),
    ("미디어경영학과", 1, "교선", True),
    ("미디어경영학과", 3, "전기", True),
    ("미디어경영학과", 4, "전선", True),
    ("소프트웨어학부", 3, "교선", True),
    ("소프트웨어학부", 4, "전필", True),  # 0개지만 전선 많음
    ("소프트웨어학부", 5, "교필", True),
    ("전자정보공학부 IT융합전공", 2, "교필", True),
    ("전자정보공학부 IT융합전공", 3, "교선", True),
    ("전자정보공학부 IT융합전공", 5, "전기", True),
    ("전자정보공학부 전자공학전공", 2, "교필", True),
    ("전자정보공학부 전자공학전공", 3, "기타", True),
    ("컴퓨터학부", 2, "채플", True),
    ("컴퓨터학부", 4, "전선", True),
    ("컴퓨터학부", 5, "교선", True),
    ("경영학부", 2, "채플", True),
    ("경영학부", 3, "전기", True),
    ("경영학부", 4, "교선", True),
    ("금융학부", 2, "전선", True),
    ("금융학부", 4, "전기", True),
    ("금융학부", 5, "교선", True),
    ("벤처경영학과", 2, "교선", True),
    ("벤처경영학과", 4, "전필", True),  # 0개, 4학년
    ("벤처경영학과", 5, "교선", True),
    ("벤처중소기업학과", 1, "교선", True),
    ("벤처중소기업학과", 1, "전선", True),
    ("벤처중소기업학과", 5, "전선", True),
    ("복지경영학과", 1, "전선", True),
    ("복지경영학과", 5, "교필", True),
    ("복지경영학과", 5, "전선", True),
    ("혁신경영학과", 1, "기타", True),
    ("혁신경영학과", 4, "전기", True),
    ("혁신경영학과", 5, "교선", True),
    ("회계세무학과", 1, "교필", True),
    ("회계세무학과", 1, "전선", True),
    ("회계세무학과", 1, "기타", True),
    ("회계학과", 1, "전필", True),
    ("회계학과", 2, "기타", True),
    ("회계학과", 5, "교필", True),
    ("경제학과", 1, "교필", True),
    ("경제학과", 1, "전선", True),  # 0개지만 2학년에 있음
    ("경제학과", 3, "전기", True),
    ("국제무역학과", 1, "전선", True),
    ("국제무역학과", 2, "교선", True),
    ("글로벌통상학과", 2, "교선", True),
    ("글로벌통상학과", 2, "교필", True),
    ("글로벌통상학과", 5, "전기", True),
    ("금융경제학과", 2, "교필", True),
    ("금융경제학과", 4, "전필", True),
    ("통상산업학과", 1, "교선", True),
    ("통상산업학과", 2, "전기", True),

    # Inappropriate tests [!]
    ("정보보호학과", 2, "전필", False),
    ("정보보호학과", 4, "전선", False),
    ("국제무역학과", 3, "전필", False),
    ("금융경제학과", 1, "전필", False),
    ("전자정보공학부 전자공학전공", 1, "전필", False),
]

def update_checklist():
    content = CHECKLIST_FILE.read_text(encoding='utf-8')
    lines = content.split('\n')

    current_dept = None
    updated_count = 0

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
                                updated_count += 1

                    # Reconstruct line
                    lines[i] = '|'.join(parts)

    # Write back
    CHECKLIST_FILE.write_text('\n'.join(lines), encoding='utf-8')
    print(f"\nTotal updated: {updated_count} items")
    print(f"Checklist updated: {CHECKLIST_FILE}")

if __name__ == "__main__":
    update_checklist()
