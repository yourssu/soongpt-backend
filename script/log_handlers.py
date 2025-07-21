class LogHandlers:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier
        
        # 로그 패턴 정의
        self.SERVER_RESTART = 'INFO org.springframework.boot.web.embedded.tomcat.TomcatWebServer - Tomcat started on port'
        self.INTERNAL_ERROR_LOG_PREFIX = 'ERROR com.yourssu.signal.handler.InternalServerErrorControllerAdvice -'
        
        # 핸들러 매핑 (로그 전용)
        self.handlers = {
            self.SERVER_RESTART: self.create_server_restart_message,
            self.INTERNAL_ERROR_LOG_PREFIX: self.create_internal_error_message,
        }
    
    def create_server_restart_message(self, line):
        """서버 재시작 메시지 생성 예시"""
        message = f"🟢 {self.config.environment.upper()} SERVER RESTARTED - 숭피티 API"
        self.notifier.send_notification(message)

    def create_internal_error_message(self, line):
        """내부 에러 메시지 생성 예시"""
        message = f"🚨ALERT ERROR - {self.config.environment.upper()} SERVER🚨\n{line.replace(self.INTERNAL_ERROR_LOG_PREFIX, '')}"
        self.notifier.send_log_notification(message)

