#!/usr/bin/env python3
import json

with open('result/target_validator/2025_2학기_target_validated.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

print('=== 기타로 변경된 예시 ===')
count = 0
for original, info in data.items():
    before = info['before_validation']
    after = info['after_validation']
    if before != after and any('기타' in x for x in after):
        print(f'{original[:50]}... -> {after[:3]}')
        count += 1
        if count >= 5:
            break

print('\n=== 단과대학 확장 예시 ===')
count = 0
for original, info in data.items():
    before = info['before_validation']
    after = info['after_validation']
    if before != after and len(after) > len(before):
        print(f'{original[:50]}...')
        print(f'  Before: {before[:3]}...')
        print(f'  After: {after[:3]}...')
        count += 1
        if count >= 3:
            break

print('\n=== 변경 통계 ===')
total_changed = 0
total_converted_to_gita = 0
total_expanded = 0

for original, info in data.items():
    before = info['before_validation']
    after = info['after_validation']
    if before != after:
        total_changed += 1
        if any('기타' in x for x in after):
            total_converted_to_gita += 1
        if len(after) > len(before):
            total_expanded += 1

print(f'총 변경된 항목: {total_changed}')
print(f'기타로 변경된 항목: {total_converted_to_gita}')
print(f'확장된 항목: {total_expanded}')