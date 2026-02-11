#!/usr/bin/env python3
"""
Run unchecked items in TEST_CHECKLIST.md
- Executes query_courses.py for unchecked [ ] items
- Reviews results and marks appropriateness
- Marks appropriate results as [x]
- Marks inappropriate/problematic results as [✗] and continues
"""

import re
import subprocess
import sys
import os
from pathlib import Path

CHECKLIST_FILE = Path(__file__).parent / "qa" / "checklists" / "TEST_CHECKLIST.md"
QUERY_SCRIPT = Path(__file__).parent / "query_courses.py"

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
            # This is a grade row like: | **1학년** | [ ] | [ ] | ...
            parts = [p.strip() for p in line.split('|')]
            if len(parts) >= 9:  # Should have grade + 7 categories
                grade_text = parts[1]  # e.g., "**1학년**"
                grade_match = re.search(r'(\d+)학년', grade_text)
                if grade_match:
                    grade = int(grade_match.group(1))

                    # Categories: 전필, 전선, 전기, 교필, 교선, 채플, 기타
                    categories = ['전필', '전선', '전기', '교필', '교선', '채플', '기타']

                    for idx, cat in enumerate(categories):
                        checkbox = parts[idx + 2]  # Skip first two columns
                        if '[ ]' in checkbox:  # Unchecked
                            tests.append({
                                'college': current_college,
                                'dept': current_dept,
                                'grade': grade,
                                'category': cat,
                                'line_num': i,
                                'checkbox_idx': idx + 2,
                                'status': 'pending'
                            })

        i += 1

    return tests, lines

def run_test(dept, grade, category):
    """Run a single test using query_courses.py"""
    cmd = [
        'python3',
        str(QUERY_SCRIPT),
        '--dept', dept,
        '--grade', str(grade),
        '--category', category
    ]

    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30
        )

        return {
            'returncode': result.returncode,
            'stdout': result.stdout,
            'stderr': result.stderr
        }

    except subprocess.TimeoutExpired:
        return {
            'returncode': -1,
            'stdout': '',
            'stderr': 'Timeout expired'
        }
    except Exception as e:
        return {
            'returncode': -1,
            'stdout': '',
            'stderr': str(e)
        }

def review_result(test, result):
    """
    Review the test result and determine if it's appropriate
    Returns: (is_appropriate: bool, reason: str, output_summary: str)
    """
    dept = test['dept']
    grade = test['grade']
    category = test['category']

    # Extract output info
    stdout = result['stdout']
    stderr = result['stderr']
    returncode = result['returncode']

    # Check for execution errors
    if returncode != 0:
        return False, f"Execution failed (code {returncode})", stderr

    if "Error" in stderr:
        return False, f"Error in stderr", stderr

    # Check for database/query errors
    if "Error connecting to database" in stdout:
        return False, "Database connection error", stdout

    if "Department" in stdout and "not found" in stdout:
        return False, "Department not found", stdout

    if "Invalid category" in stdout:
        return False, "Invalid category", stdout

    # Extract course count
    count_match = re.search(r'Found (\d+) courses', stdout)
    if not count_match:
        return False, "Could not parse result", stdout

    count = int(count_match.group(1))

    # Review appropriateness based on expected patterns
    # This is a basic heuristic - adjust as needed

    # For certain categories, we expect results
    if category in ['교필', '교선']:
        # General education courses should be available to most departments
        # 0 results might be acceptable for higher grades
        if count == 0 and grade <= 2:
            return False, f"No {category} courses found for grade {grade}", f"Found 0 courses"

    # For major courses
    elif category in ['전필', '전선', '전기']:
        # It's acceptable to have 0 results for some dept/grade/category combinations
        # but we should at least verify the query ran correctly
        pass

    # If we got here, the test executed successfully
    return True, f"Query successful, found {count} courses", f"Found {count} courses"

def update_checklist_line(lines, test, is_appropriate):
    """Update a specific checkbox in the checklist"""
    line_num = test['line_num']
    line = lines[line_num]
    checkbox_idx = test['checkbox_idx']

    parts = line.split('|')
    if len(parts) >= checkbox_idx + 1:
        if is_appropriate:
            parts[checkbox_idx] = ' [x] '
        else:
            parts[checkbox_idx] = ' [✗] '

        lines[line_num] = '|'.join(parts)
        return True

    return False

def main():
    import argparse
    parser = argparse.ArgumentParser(description='Run unchecked tests from TEST_CHECKLIST.md')
    parser.add_argument('--yes', '-y', action='store_true', help='Skip confirmation prompt')
    args = parser.parse_args()

    if not CHECKLIST_FILE.exists():
        print(f"Error: {CHECKLIST_FILE} not found")
        sys.exit(1)

    if not QUERY_SCRIPT.exists():
        print(f"Error: {QUERY_SCRIPT} not found")
        sys.exit(1)

    # Read checklist
    print(f"Reading checklist from {CHECKLIST_FILE}")
    content = CHECKLIST_FILE.read_text(encoding='utf-8')

    # Parse tests
    tests, lines = parse_checklist(content)
    print(f"\nFound {len(tests)} unchecked tests")

    if not tests:
        print("No unchecked tests found. All tests are complete!")
        return

    # Show first few tests
    print("\nFirst 5 tests:")
    for i, test in enumerate(tests[:5], 1):
        print(f"  {i}. {test['dept']} - {test['grade']}학년 - {test['category']}")

    if len(tests) > 5:
        print(f"  ... and {len(tests) - 5} more")

    # Ask for confirmation
    if not args.yes:
        print(f"\nWill run {len(tests)} tests. Continue? [y/N] ", end='')
        response = input().strip().lower()
        if response != 'y':
            print("Cancelled.")
            return
    else:
        print(f"\nRunning {len(tests)} tests (auto-confirmed with --yes)...")

    # Run tests
    results = []
    for i, test in enumerate(tests, 1):
        print(f"\n[{i}/{len(tests)}] {test['dept']} - {test['grade']}학년 - {test['category']}")

        # Run the test
        result = run_test(test['dept'], test['grade'], test['category'])

        # Review the result
        is_appropriate, reason, summary = review_result(test, result)

        if is_appropriate:
            print(f"  ✅ APPROPRIATE: {summary}")
        else:
            print(f"  ✗ INAPPROPRIATE: {reason}")
            if summary:
                print(f"     {summary}")

        # Update the checklist
        update_checklist_line(lines, test, is_appropriate)

        results.append({
            'test': test,
            'appropriate': is_appropriate,
            'reason': reason
        })

        # Save after each test
        CHECKLIST_FILE.write_text('\n'.join(lines), encoding='utf-8')

    # Summary
    appropriate = sum(1 for r in results if r['appropriate'])
    inappropriate = len(results) - appropriate

    print(f"\n{'='*80}")
    print("SUMMARY")
    print('='*80)
    print(f"Total Tests: {len(results)}")
    print(f"Appropriate: {appropriate}")
    print(f"Inappropriate: {inappropriate}")

    if inappropriate > 0:
        print("\nInappropriate Tests:")
        for r in results:
            if not r['appropriate']:
                t = r['test']
                print(f"  ✗ {t['dept']} - {t['grade']}학년 - {t['category']}")
                print(f"     Reason: {r['reason']}")

    print(f"\nChecklist updated: {CHECKLIST_FILE}")

if __name__ == "__main__":
    main()
