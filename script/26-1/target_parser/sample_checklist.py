#!/usr/bin/env python3
"""
Sample unchecked items from TEST_CHECKLIST.md for manual review
"""

import re
from pathlib import Path
import random

CHECKLIST_FILE = Path(__file__).parent / "TEST_CHECKLIST.md"

def parse_checklist(content):
    """Parse the checklist and extract test information"""
    lines = content.split('\n')
    tests = []
    current_dept = None
    current_college = None

    i = 0
    while i < len(lines):
        line = lines[i]

        # Track college
        if line.startswith('### ') and not line.startswith('####'):
            current_college = line.replace('### ', '').strip()

        # Track department
        elif line.startswith('#### '):
            current_dept = line.replace('#### ', '').strip()

        # Parse table rows with checkboxes
        elif '|' in line and '**' in line and '학년' in line:
            parts = [p.strip() for p in line.split('|')]
            if len(parts) >= 9:
                grade_text = parts[1]
                grade_match = re.search(r'(\d+)학년', grade_text)
                if grade_match:
                    grade = int(grade_match.group(1))
                    categories = ['전필', '전선', '전기', '교필', '교선', '채플', '기타']

                    for idx, cat in enumerate(categories):
                        checkbox = parts[idx + 2]
                        if '[ ]' in checkbox:
                            tests.append({
                                'college': current_college,
                                'dept': current_dept,
                                'grade': grade,
                                'category': cat,
                                'line_num': i,
                                'checkbox_idx': idx + 2,
                            })

        i += 1

    return tests

def sample_tests(tests, samples_per_dept=2):
    """Sample tests from each department"""
    # Group by department
    by_dept = {}
    for test in tests:
        dept = test['dept']
        if dept not in by_dept:
            by_dept[dept] = []
        by_dept[dept].append(test)

    # Sample from each department
    sampled = []
    for dept, dept_tests in by_dept.items():
        # Prioritize: 전필/전선 (major courses) and 교필/교선 (general ed)
        major_tests = [t for t in dept_tests if t['category'] in ['전필', '전선', '전기']]
        general_tests = [t for t in dept_tests if t['category'] in ['교필', '교선']]

        # Sample 1 major and 1 general if possible
        sample = []
        if major_tests:
            sample.append(random.choice(major_tests))
        if general_tests and len(sample) < samples_per_dept:
            sample.append(random.choice(general_tests))

        # Fill remaining slots
        remaining = samples_per_dept - len(sample)
        if remaining > 0:
            other_tests = [t for t in dept_tests if t not in sample]
            if other_tests:
                sample.extend(random.sample(other_tests, min(remaining, len(other_tests))))

        sampled.extend(sample)

    return sampled

def main():
    content = CHECKLIST_FILE.read_text(encoding='utf-8')
    tests = parse_checklist(content)

    print(f"Total unchecked tests: {len(tests)}")

    # Group by department
    by_dept = {}
    for test in tests:
        dept = test['dept']
        if dept not in by_dept:
            by_dept[dept] = []
        by_dept[dept].append(test)

    print(f"Departments with unchecked tests: {len(by_dept)}")
    print()

    # Sample
    sampled = sample_tests(tests, samples_per_dept=3)
    print(f"Sampled {len(sampled)} tests for review:")
    print()

    for i, test in enumerate(sampled, 1):
        print(f"{i}. {test['dept']} - {test['grade']}학년 - {test['category']}")
        print(f"   Command: python3 query_courses.py --dept \"{test['dept']}\" --grade {test['grade']} --category \"{test['category']}\"")
        print()

if __name__ == "__main__":
    main()