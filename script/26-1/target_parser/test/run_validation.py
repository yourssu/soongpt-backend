#!/usr/bin/env python3
"""
수강대상 파싱 검증 자동화 스크립트

REVIEW_GUIDELINES.md에 따라 전체 검증 워크플로우를 실행합니다:
1. transform_targets.py - 파싱
2. analyze_results.py - 분석
3. generate_summary.py - HTML 리포트 생성
4. convert_for_flashcard.py - Flashcard 변환

Usage:
    cd test
    python run_validation.py
"""

import subprocess
import sys
import os
from datetime import datetime

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))  # test directory
PARENT_DIR = os.path.dirname(BASE_DIR)  # target_parser directory
TEST_DIR = BASE_DIR  # We're in test directory
VENV_PYTHON = os.path.join(PARENT_DIR, "venv", "bin", "python")

# Check if we should use venv or system python
if os.path.exists(VENV_PYTHON):
    PYTHON = VENV_PYTHON
    print("✓ Using virtual environment Python")
else:
    PYTHON = sys.executable
    print("⚠ Virtual environment not found, using system Python")

def run_command(script_path, description, cwd=None):
    """Run a Python script and handle errors."""
    print(f"\n{'='*80}")
    print(f"🔄 {description}")
    print(f"{'='*80}\n")

    try:
        result = subprocess.run(
            [PYTHON, script_path],
            cwd=cwd or BASE_DIR,
            capture_output=False,
            text=True,
            check=True
        )
        print(f"\n✅ {description} - 완료")
        return True
    except subprocess.CalledProcessError as e:
        print(f"\n❌ {description} - 실패")
        print(f"Error: {e}")
        return False
    except Exception as e:
        print(f"\n❌ {description} - 오류 발생")
        print(f"Error: {e}")
        return False

def main():
    print("""
╔════════════════════════════════════════════════════════════════════════════╗
║                     수강대상 파싱 검증 자동화 시스템                        ║
║                                                                              ║
║  REVIEW_GUIDELINES.md 기반 전체 워크플로우 실행                            ║
╚════════════════════════════════════════════════════════════════════════════╝
    """)

    start_time = datetime.now()

    # Step 1: Run transform_targets.py (in parent directory)
    step1 = run_command(
        os.path.join(PARENT_DIR, "transform_targets.py"),
        "Step 1: 파싱 (transform_targets.py)",
        cwd=PARENT_DIR
    )

    if not step1:
        print("\n⛔ 파싱 단계 실패. 중단합니다.")
        sys.exit(1)

    # Step 2: Run analyze_results.py
    step2 = run_command(
        os.path.join(TEST_DIR, "analyze_results.py"),
        "Step 2: 분석 (analyze_results.py)",
        cwd=TEST_DIR
    )

    if not step2:
        print("\n⛔ 분석 단계 실패. 중단합니다.")
        sys.exit(1)

    # Step 3: Run generate_summary.py
    step3 = run_command(
        os.path.join(TEST_DIR, "generate_summary.py"),
        "Step 3: HTML 리포트 생성 (generate_summary.py)",
        cwd=TEST_DIR
    )

    if not step3:
        print("\n⚠️  HTML 리포트 생성 실패. 계속 진행합니다.")

    # Step 4: Run convert_for_flashcard.py
    step4 = run_command(
        os.path.join(TEST_DIR, "convert_for_flashcard.py"),
        "Step 4: Flashcard 변환 (convert_for_flashcard.py)",
        cwd=TEST_DIR
    )

    if not step4:
        print("\n⛔ Flashcard 변환 실패.")
        sys.exit(1)

    # Step 5: Prepare for Claude review
    step5 = run_command(
        os.path.join(TEST_DIR, "prepare_claude_review.py"),
        "Step 5: Claude 검증 준비 (prepare_claude_review.py)",
        cwd=TEST_DIR
    )

    if not step5:
        print("\n⚠️  Claude 검증 준비 실패. 계속 진행합니다.")

    # Summary
    end_time = datetime.now()
    duration = (end_time - start_time).total_seconds()

    print(f"\n\n{'='*80}")
    print(f"✅ 전체 검증 워크플로우 완료!")
    print(f"{'='*80}")
    print(f"⏱️  총 소요 시간: {duration:.2f}초")

    # Find the latest timestamp folder
    timestamp_folders = [d for d in os.listdir(TEST_DIR)
                        if os.path.isdir(os.path.join(TEST_DIR, d))
                        and d[0].isdigit()]

    if timestamp_folders:
        latest = sorted(timestamp_folders)[-1]
        result_dir = os.path.join(TEST_DIR, latest)

        print(f"\n📁 결과 파일 위치:")
        print(f"   {result_dir}")
        print(f"\n📊 생성된 파일:")
        print(f"   ├── suspicious_parsing.json       (상세 분석 결과)")
        print(f"   ├── suspicious_for_review.json    (Flashcard 리뷰용)")
        print(f"   ├── analysis_report.html          (HTML 리포트)")
        print(f"   └── README.md                     (사용 방법)")

        review_ready_path = os.path.join(result_dir, "review_ready.json")
        instruction_path = os.path.join(result_dir, "CLAUDE_REVIEW_INSTRUCTION.md")

        print(f"\n🤖 Claude 자동 검증 (권장):")
        print(f"   Claude에게 다음과 같이 요청:")
        print(f'   "REVIEW_GUIDELINES.md에 따라 {review_ready_path}을 검증하고')
        print(f'    {result_dir}/claude_validated.json으로 저장해주세요"')

        print(f"\n📖 또는 수동 검토:")
        flashcard_path = os.path.join(result_dir, "suspicious_for_review.json")
        print(f"   1. flashcard_review.html을 브라우저에서 열기")
        print(f"   2. '📂 열기' 버튼 클릭")
        print(f"   3. {flashcard_path} 파일 선택")
        print(f"   4. 항목별 수동 검토")

    print(f"\n{'='*80}\n")

if __name__ == "__main__":
    main()
