#!/bin/bash

PROJECT_NAME=soongpt
MAX_RETRIES=2
retry_count=0

check_and_start_application() {
    if [ $retry_count -ge $MAX_RETRIES ]; then
        echo "최대 재시도 횟수($MAX_RETRIES)에 도달했습니다. 스크립트를 종료합니다."
        exit 1
    fi

    if pgrep -f "yourssu-$PROJECT_NAME-application\.jar" > /dev/null; then
        echo "기존 프로세스 종료 중..."
        sudo ps -ef | grep "[j]ava .*yourssu-$PROJECT_NAME-application\.jar" | awk '{print $2}' | xargs sudo kill -9
    else
        echo "실행 중인 프로세스 없음"
    fi

    echo "애플리케이션 시작 중... (시도 $((retry_count + 1))/$MAX_RETRIES)"

    nohup sudo /usr/lib/jvm/java-21-openjdk-arm64/bin/java -Duser.timezone=Asia/Seoul -Dspring.profiles.active=$ENVIRONMENT -Xmx1024M -Xms1024M -jar yourssu-$PROJECT_NAME-application.jar > /dev/null 2>&1 &


    echo "프로세스 상태 확인을 위해 20초 대기 중..."
    sleep 10

    for i in $(seq 0 9); do
    if pgrep -f "yourssu-$PROJECT_NAME-application\.jar" > /dev/null; then
        echo "애플리케이션이 정상적으로 실행되었습니다."
        exit 0
    fi
    sleep 1
    done

    echo "애플리케이션 실행 실패. 재시도 중..."
    retry_count=$((retry_count + 1))
    check_and_start_application
}

cd /home/ubuntu/$PROJECT_NAME-api
if [ -f ".env" ]; then
    set -a  # 자동으로 모든 변수를 export
    . ./.env
    set +a
fi
check_and_start_application
