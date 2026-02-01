import csv
import json
from collections import Counter

# CSV 읽기
input_file = 'ssu26-1.csv'
output_file = 'unique_targets.json'

targets = []
target_counts = Counter()

with open(input_file, 'r', encoding='utf-8') as f:
    reader = csv.reader(f)
    next(reader)  # 헤더 스킵

    for row in reader:
        if len(row) >= 18:
            target = row[17].strip()  # 수강대상 컬럼
            if target:
                targets.append(target)
                target_counts[target] += 1

# 중복 제거 및 빈도 정보 추가
unique_targets = []
seen = set()

for target in targets:
    if target not in seen:
        seen.add(target)
        unique_targets.append({
            "text": target,
            "count": target_counts[target]
        })

# 빈도순으로 정렬
unique_targets.sort(key=lambda x: x["count"], reverse=True)

# JSON 저장
result = {
    "total_courses": len(targets),
    "unique_targets_count": len(unique_targets),
    "targets": unique_targets
}

with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"✅ 처리 완료")
print(f"  - 총 과목 수: {len(targets)}")
print(f"  - 고유 수강대상 패턴: {len(unique_targets)}개")
print(f"  - 출력 파일: {output_file}")
print(f"\n상위 10개 수강대상 패턴:")
for i, item in enumerate(unique_targets[:10], 1):
    print(f"  {i}. [{item['count']}건] {item['text'][:60]}...")
