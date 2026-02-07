# target_validation

수강대상 검증 파이프라인입니다.

## 핵심 목적
- `data.yml` 화이트리스트 기준 학과 검증
- 학년/범위 파싱 검증
- 파싱 누락 토큰 검증
- Gemini(`gemini-flash-3.0`) 보조 판정
- 배포 전 차단 게이트(`non-zero exit code`)

## 실행 예시
```bash
python3 script/26-1/target_validation/run_validation.py \
  --csv-path script/26-1/course/ssu26-1.csv \
  --data-yml-path src/main/resources/data.yml \
  --env-path .env
```

## 주요 옵션
- `--disable-ai`: Gemini 검증 비활성화
- `--target-column`: 수강대상 컬럼명 명시
- `--course-code-column`: 과목코드 컬럼명 명시
- `--fail-on-manual-review`: 수동검토 건 발생 시 non-zero 종료
- `--fail-on-ai-skipped`: AI 스킵 건 발생 시 non-zero 종료
- `--require-ai-invoked`: AI 호출 성공 건이 1건 이상이어야 통과
- `--min-coverage-rate`: 최소 파싱 커버리지(%) 강제

## 출력 파일
- `script/26-1/target_validation/reports/validation_report.json`
- `script/26-1/target_validation/reports/manual_review.csv`

## .env
`.env`에 아래 키가 필요합니다.

```env
gemini_api_key=YOUR_KEY
```

참고:
- `--gemini-model gemini-flash-3.0` 입력 시 내부적으로 `gemini-3-flash-preview` 모델로 매핑해서 호출합니다.

## 배포 게이트 예시
```bash
python3 script/26-1/target_validation/run_validation.py \
  --csv-path script/26-1/course/ssu26-1.csv \
  --data-yml-path src/main/resources/data.yml \
  --env-path .env \
  --gemini-model gemini-flash-3.0 \
  --ai-confidence-threshold 0.8 \
  --fail-on-manual-review \
  --fail-on-ai-skipped \
  --require-ai-invoked \
  --min-coverage-rate 99
```
