class LogHandlers:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier

        # 로그 패턴 정의
        self.SERVER_RESTART = 'INFO org.springframework.boot.web.embedded.tomcat.TomcatWebServer - Tomcat started on port'
        # 내부 서버 에러는 슬랙 알림 제외(로그만 남김). 필요 시 이 prefix로 로그 검색

        # 핸들러 매핑 (로그 전용)
        self.handlers = {
            self.SERVER_RESTART: self.create_server_restart_message,
        }

    def create_server_restart_message(self, line):
        """서버 재시작 메시지 생성 예시"""
        message = f"🟢 {self.config.environment.upper()} SERVER RESTARTED - 숭피티 API"
        self.notifier.send_log_notification(message)
