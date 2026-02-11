#!/usr/bin/env python3
"""
Generate comprehensive test checklist for course visibility validation.
Tests all combinations of department x grade x category.
"""

import yaml
import os
from datetime import datetime

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "../../../"))
DATA_YML_PATH = os.path.join(PROJECT_ROOT, "src/main/resources/data.yml")
OUTPUT_PATH = os.path.join(BASE_DIR, "qa", "checklists", "TEST_CHECKLIST.md")

# Test configurations
GRADES = [1, 2, 3, 4, 5]
CATEGORIES = ["전필", "전선", "전기", "교필", "교선", "채플", "기타"]

def load_departments():
    """Load all departments from data.yml"""
    with open(DATA_YML_PATH, 'r', encoding='utf-8') as f:
        data = yaml.safe_load(f)

    departments = []
    for college in data['ssu-data']['colleges']:
        college_name = college['name']
        for dept_name in college['departments']:
            departments.append({
                'college': college_name,
                'department': dept_name
            })

    return departments

def generate_checklist():
    """Generate markdown checklist document"""
    departments = load_departments()

    # Calculate statistics
    total_depts = len(departments)
    total_tests = total_depts * len(GRADES) * len(CATEGORIES)

    # Generate markdown
    md = []
    md.append("# Course Visibility Test Checklist")
    md.append("")
    md.append(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    md.append("")
    md.append("## Test Overview")
    md.append("")
    md.append(f"- **Total Departments**: {total_depts}")
    md.append(f"- **Grades per Department**: {len(GRADES)} (1-5)")
    md.append(f"- **Categories per Grade**: {len(CATEGORIES)}")
    md.append(f"- **Total Test Cases**: {total_tests:,}")
    md.append("")
    md.append("## Test Command Template")
    md.append("")
    md.append("```bash")
    md.append('python query_courses.py --dept "<department_name>" --grade <grade> --category "<category>"')
    md.append("```")
    md.append("")
    md.append("## Test Cases by College")
    md.append("")

    # Group departments by college
    colleges = {}
    for dept in departments:
        college_name = dept['college']
        if college_name not in colleges:
            colleges[college_name] = []
        colleges[college_name].append(dept['department'])

    # Generate checklist for each college
    for college_name in sorted(colleges.keys()):
        md.append(f"### {college_name}")
        md.append("")

        for dept_name in sorted(colleges[college_name]):
            md.append(f"#### {dept_name}")
            md.append("")

            # Create a table for this department
            md.append("| Grade | 전필 | 전선 | 전기 | 교필 | 교선 | 채플 | 기타 |")
            md.append("|-------|------|------|------|------|------|------|------|")

            for grade in GRADES:
                row = [f"**{grade}학년**"]
                for category in CATEGORIES:
                    row.append("[ ]")
                md.append("| " + " | ".join(row) + " |")

            md.append("")

            # Add command examples for this department
            md.append("<details>")
            md.append("<summary>Test Commands</summary>")
            md.append("")
            md.append("```bash")
            for grade in GRADES:
                for category in CATEGORIES:
                    cmd = f'python query_courses.py --dept "{dept_name}" --grade {grade} --category "{category}"'
                    md.append(cmd)
            md.append("```")
            md.append("")
            md.append("</details>")
            md.append("")

    # Add testing instructions
    md.append("## Testing Instructions")
    md.append("")
    md.append("1. Navigate to the script directory:")
    md.append("   ```bash")
    md.append("   cd script/26-1/target_parser")
    md.append("   ```")
    md.append("")
    md.append("2. Run each test command and verify:")
    md.append("   - Query executes without errors")
    md.append("   - Results match expected visibility rules")
    md.append("   - Target information is correctly applied")
    md.append("")
    md.append("3. Mark checkbox with `[x]` after verifying each test case")
    md.append("")
    md.append("## Expected Behaviors")
    md.append("")
    md.append("### Allow Rules")
    md.append("- Course appears if there's a matching ALLOW rule for the student's scope")
    md.append("- Scope matching: University (all) > College (same college) > Department (same dept)")
    md.append("")
    md.append("### Deny Rules")
    md.append("- Course is hidden if there's a matching DENY rule, even with ALLOW rules")
    md.append("- DENY rules take precedence over ALLOW rules")
    md.append("")
    md.append("### Category Matching")
    md.append("- Course category must match the query category")
    md.append("- 전필 (MAJOR_REQUIRED), 전선 (MAJOR_ELECTIVE), 전기 (MAJOR_BASIC)")
    md.append("- 교필 (GENERAL_REQUIRED), 교선 (GENERAL_ELECTIVE)")
    md.append("- 채플 (CHAPEL), 기타 (OTHER)")
    md.append("")
    md.append("## Notes")
    md.append("")
    md.append("- Empty results are valid if no courses match the criteria")
    md.append("- Focus on verifying the target scope logic is working correctly")
    md.append("- Check for any unexpected course appearances or disappearances")

    return "\n".join(md)

def main():
    print("Generating test checklist...")

    checklist_content = generate_checklist()

    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        f.write(checklist_content)

    print(f"✓ Checklist generated: {OUTPUT_PATH}")

    departments = load_departments()
    total_tests = len(departments) * len(GRADES) * len(CATEGORIES)
    print(f"✓ Total test cases: {total_tests:,}")

if __name__ == "__main__":
    main()
