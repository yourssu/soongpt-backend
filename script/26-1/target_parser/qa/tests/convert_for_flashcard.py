import json
import os
import shutil
from datetime import datetime

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

# Extract timestamp from filename (e.g., suspicious_parsing_20260201_135954.json)
timestamp = latest_file.replace("suspicious_parsing_", "").replace(".json", "")

print(f"Converting: {latest_file}")
print(f"Timestamp: {timestamp}")

# Create timestamped folder
timestamp_dir = os.path.join(TEST_DIR, timestamp)
os.makedirs(timestamp_dir, exist_ok=True)
print(f"Created directory: {timestamp_dir}")

# Load suspicious data
with open(suspicious_path, 'r', encoding='utf-8') as f:
    suspicious_data = json.load(f)

# Extract suspicious items
suspicious_items = suspicious_data["suspicious_items"]

# Convert to flashcard-compatible format
flashcard_data = []

for item in suspicious_items:
    confidence = item.get("confidence", "unknown")
    issues = item.get("issues", [])

    # Create flashcard entry with metadata
    flashcard_entry = {
        "original_text": item["original_text"],
        "count": item["count"],
        "parsed_targets": item["parsed_targets"],
        "_metadata": {
            "confidence": confidence,
            "issues": [issue.get("type") for issue in issues],
            "issue_details": issues
        }
    }

    flashcard_data.append(flashcard_entry)

# Sort by confidence (low first, as they need more attention)
confidence_order = {"low": 0, "medium": 1, "high": 2}
flashcard_data.sort(key=lambda x: confidence_order.get(x["_metadata"]["confidence"], 3))

# Save to flashcard-compatible JSON in timestamped folder
output_path = os.path.join(timestamp_dir, "suspicious_for_review.json")

with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(flashcard_data, f, ensure_ascii=False, indent=2)

print(f"\n✅ Flashcard JSON saved: {output_path}")

# Move or copy existing files to timestamped folder
files_to_organize = [
    (suspicious_path, os.path.join(timestamp_dir, "suspicious_parsing.json")),
]

# Find HTML report with same timestamp
html_report = os.path.join(TEST_DIR, f"analysis_report_{timestamp}.html")
if os.path.exists(html_report):
    files_to_organize.append(
        (html_report, os.path.join(timestamp_dir, "analysis_report.html"))
    )

# Copy files to timestamped folder
for src, dst in files_to_organize:
    if os.path.exists(src):
        shutil.copy2(src, dst)
        print(f"Copied: {os.path.basename(src)} -> {dst}")

# Create README for the timestamped folder
readme_path = os.path.join(timestamp_dir, "README.md")
readme_content = f"""# 파싱 검증 결과 - {timestamp}

## 생성 시간
{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## 파일 목록

### 1. suspicious_for_review.json
- **용도**: Flashcard 리뷰용 JSON
- **항목 수**: {len(flashcard_data)}
- **사용법**: flashcard_review.html에서 이 파일을 열어 검토

### 2. suspicious_parsing.json
- **용도**: 상세 분석 결과 (메타데이터 포함)
- **내용**: confidence, issues 등 분석 정보

### 3. analysis_report.html
- **용도**: HTML 분석 리포트
- **내용**: 이슈 타입별 예시 및 통계

## 통계

- **전체 의심 항목**: {len(flashcard_data)}
- **Low confidence**: {len([x for x in flashcard_data if x['_metadata']['confidence'] == 'low'])}
- **Medium confidence**: {len([x for x in flashcard_data if x['_metadata']['confidence'] == 'medium'])}
- **High confidence**: {len([x for x in flashcard_data if x['_metadata']['confidence'] == 'high'])}

## 사용 방법

1. `flashcard_review.html`을 브라우저에서 열기
2. "📂 열기" 버튼 클릭
3. `suspicious_for_review.json` 파일 선택
4. 검토 후 오류 항목 표시 (E 키)
5. 결과 리포트 확인 (U 키)
"""

with open(readme_path, 'w', encoding='utf-8') as f:
    f.write(readme_content)

print(f"Created: README.md")

print(f"\n" + "="*80)
print(f"📁 모든 파일이 정리되었습니다: {timestamp_dir}")
print(f"="*80)
print(f"\n📊 통계:")
print(f"  - 전체 항목: {len(flashcard_data)}")
print(f"  - Low:    {len([x for x in flashcard_data if x['_metadata']['confidence'] == 'low'])}")
print(f"  - Medium: {len([x for x in flashcard_data if x['_metadata']['confidence'] == 'medium'])}")
print(f"  - High:   {len([x for x in flashcard_data if x['_metadata']['confidence'] == 'high'])}")

print(f"\n📖 다음 단계:")
print(f"1. flashcard_review.html을 브라우저에서 열기")
print(f"2. 파일 열기: {output_path}")
