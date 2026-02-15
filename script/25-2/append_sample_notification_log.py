#!/usr/bin/env python3
"""
observer 동작만 빠르게 테스트할 때 사용.
logs/spring.log 에 예시 알림 로그를 한 줄 append 합니다.
프로젝트 루트에서 실행: python script/25-2/append_sample_notification_log.py

실행 후 observer.py 가 이미 떠 있으면, 해당 줄을 읽어 Slack 으로 전송합니다.
"""
import os
import sys
from datetime import datetime

# 프로젝트 루트 = script/25-2 의 상위 두 단계
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(SCRIPT_DIR))
LOG_DIR = os.path.join(ROOT, "logs")
SPRING_LOG = os.path.join(LOG_DIR, "spring.log")

# logback FILE_LOG_PATTERN: %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %5level %logger - %msg%n
def log_line(level: str, logger: str, msg: str) -> str:
    ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S.000")
    return f"{ts} [main] {level:5} {logger} - {msg}\n"


def main():
    os.makedirs(LOG_DIR, exist_ok=True)

    # soongpt_handler 에서 감지하는 prefix 와 동일한 문구로 예시 로그 작성
    samples = {
        "student": (
            "WARN ", "com.yourssu.soongpt.common.infrastructure.notification.Notification",
            'StudentInfoMappingAlert&{"studentIdPrefix":"2024","failureReason":"로컬 테스트: 학과 매칭 실패"}',
        ),
        "rusaint": (
            "WARN ", "com.yourssu.soongpt.common.infrastructure.notification.Notification",
            'RusaintServiceError&{"operation":"graduation","statusCode":500,"errorMessage":"로컬 테스트 에러","studentIdPrefix":"2024"}',
        ),
        "graduation": (
            "WARN ", "com.yourssu.soongpt.common.infrastructure.notification.Notification",
            "GraduationSummaryAlert&{departmentName:컴퓨터학부,userGrade:3,missingItems:전공선택(MAJOR_ELECTIVE);교양필수(GENERAL_REQUIRED)}",
        ),
    }

    key = sys.argv[1] if len(sys.argv) > 1 else "student"
    if key not in samples:
        print(f"Usage: {sys.argv[0]} [student|rusaint|graduation]")
        print(f"  student (default), rusaint, graduation")
        sys.exit(1)
    line = log_line(*samples[key])

    with open(SPRING_LOG, "a", encoding="utf-8") as f:
        f.write(line)
    print(f"Appended 1 line ({key}) to {SPRING_LOG}")
    print("If observer.py is running, it should send this to Slack.")


if __name__ == "__main__":
    main()
