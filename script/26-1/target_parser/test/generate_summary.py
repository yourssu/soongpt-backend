import json
import os
from collections import defaultdict

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TEST_DIR = BASE_DIR  # Now we're in the test directory

# Find the most recent suspicious parsing file
suspicious_files = [f for f in os.listdir(TEST_DIR) if f.startswith("suspicious_parsing_")]
if not suspicious_files:
    print("No suspicious parsing files found!")
    exit(1)

latest_file = sorted(suspicious_files)[-1]
suspicious_path = os.path.join(TEST_DIR, latest_file)

print(f"Analyzing: {latest_file}")

# Load data
with open(suspicious_path, 'r', encoding='utf-8') as f:
    data = json.load(f)

metadata = data["metadata"]
suspicious_items = data["suspicious_items"]

# Group by issue type
issue_type_examples = defaultdict(list)
issue_type_counts = defaultdict(int)

for item in suspicious_items:
    for issue in item.get("issues", []):
        issue_type = issue["type"]
        issue_type_counts[issue_type] += 1

        # Keep only first 5 examples per type
        if len(issue_type_examples[issue_type]) < 5:
            issue_type_examples[issue_type].append({
                "original_text": item["original_text"],
                "count": item["count"],
                "confidence": item["confidence"],
                "issue_detail": issue["detail"],
                "parsed_targets": item["parsed_targets"]
            })

# Print summary
print("\n" + "="*80)
print("ANALYSIS SUMMARY")
print("="*80)
print(f"\nTotal targets: {metadata['total_targets']}")
print(f"Suspicious items: {metadata['suspicious_count']} ({metadata['suspicious_count']/metadata['total_targets']*100:.1f}%)")
print(f"\nConfidence breakdown:")
print(f"  - Low:    {metadata['confidence_breakdown']['low']}")
print(f"  - Medium: {metadata['confidence_breakdown']['medium']}")
print(f"  - High:   {metadata['confidence_breakdown']['high']}")

print("\n" + "="*80)
print("ISSUE TYPES BREAKDOWN")
print("="*80)

for issue_type, count in sorted(issue_type_counts.items(), key=lambda x: x[1], reverse=True):
    print(f"\n{issue_type}: {count} occurrences")
    print("-" * 80)

    examples = issue_type_examples[issue_type]
    for i, example in enumerate(examples, 1):
        print(f"\n  Example {i}:")
        print(f"    Original: {example['original_text'][:100]}...")
        print(f"    Count: {example['count']}")
        print(f"    Confidence: {example['confidence']}")
        print(f"    Issue: {example['issue_detail'][:150]}...")
        print(f"    Parsed count: {len(example['parsed_targets'])} target(s)")

# Generate detailed HTML report
html_path = os.path.join(TEST_DIR, f"analysis_report_{metadata['timestamp']}.html")

html_content = f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Target Parsing Analysis Report</title>
    <style>
        body {{ font-family: 'Noto Sans KR', Arial, sans-serif; margin: 20px; background: #f5f5f5; }}
        .container {{ max-width: 1200px; margin: 0 auto; background: white; padding: 20px; }}
        h1, h2, h3 {{ color: #333; }}
        .summary {{ background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; }}
        .issue-section {{ margin: 30px 0; border: 1px solid #ddd; padding: 15px; border-radius: 5px; }}
        .example {{ background: #f9f9f9; padding: 10px; margin: 10px 0; border-left: 3px solid #2196F3; }}
        .confidence-low {{ border-left-color: #f44336; }}
        .confidence-medium {{ border-left-color: #ff9800; }}
        .confidence-high {{ border-left-color: #4caf50; }}
        .original {{ font-weight: bold; color: #1976d2; }}
        .parsed {{ background: #fff3e0; padding: 5px; margin: 5px 0; font-size: 0.9em; }}
        pre {{ background: #263238; color: #aed581; padding: 10px; overflow-x: auto; border-radius: 3px; }}
        .stat {{ display: inline-block; margin: 10px 20px 10px 0; }}
        .stat-label {{ font-weight: bold; }}
        .stat-value {{ font-size: 1.5em; color: #1976d2; }}
    </style>
</head>
<body>
    <div class="container">
        <h1>🔍 Target Parsing Analysis Report</h1>
        <p>Generated: {metadata['timestamp']}</p>

        <div class="summary">
            <h2>Summary</h2>
            <div class="stat">
                <span class="stat-label">Total Targets:</span>
                <span class="stat-value">{metadata['total_targets']}</span>
            </div>
            <div class="stat">
                <span class="stat-label">Suspicious:</span>
                <span class="stat-value">{metadata['suspicious_count']}</span>
                <span>({metadata['suspicious_count']/metadata['total_targets']*100:.1f}%)</span>
            </div>
            <h3>Confidence Breakdown</h3>
            <div class="stat">
                <span class="stat-label">Low:</span>
                <span class="stat-value" style="color: #f44336;">{metadata['confidence_breakdown']['low']}</span>
            </div>
            <div class="stat">
                <span class="stat-label">Medium:</span>
                <span class="stat-value" style="color: #ff9800;">{metadata['confidence_breakdown']['medium']}</span>
            </div>
            <div class="stat">
                <span class="stat-label">High:</span>
                <span class="stat-value" style="color: #4caf50;">{metadata['confidence_breakdown']['high']}</span>
            </div>
        </div>
"""

# Add issue sections
for issue_type, count in sorted(issue_type_counts.items(), key=lambda x: x[1], reverse=True):
    html_content += f"""
        <div class="issue-section">
            <h2>📌 {issue_type}</h2>
            <p><strong>Occurrences:</strong> {count}</p>
"""

    examples = issue_type_examples[issue_type]
    for i, example in enumerate(examples, 1):
        conf_class = f"confidence-{example['confidence']}"
        html_content += f"""
            <div class="example {conf_class}">
                <h3>Example {i} (Confidence: {example['confidence']})</h3>
                <p><span class="original">Original:</span> {example['original_text']}</p>
                <p><strong>Count:</strong> {example['count']} occurrences in dataset</p>
                <p><strong>Issue:</strong> {example['issue_detail']}</p>
                <div class="parsed">
                    <strong>Parsed Result ({len(example['parsed_targets'])} target(s)):</strong>
                    <pre>{json.dumps(example['parsed_targets'], ensure_ascii=False, indent=2)}</pre>
                </div>
            </div>
"""

    html_content += "        </div>\n"

html_content += """
    </div>
</body>
</html>
"""

with open(html_path, 'w', encoding='utf-8') as f:
    f.write(html_content)

print(f"\n\nHTML report generated: {html_path}")
