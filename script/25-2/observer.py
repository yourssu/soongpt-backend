import time
import os
import requests
import pytz
from datetime import datetime
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from dotenv import load_dotenv
from log_handlers import LogHandlers
from soongpt_handler import SoongptHandler

class Config:
    def __init__(self):
        load_dotenv(override=True)
        self.environment = os.getenv('ENVIRONMENT')
        self.slack_token = os.getenv('SLACK_TOKEN')
        self.slack_channel = os.getenv('SLACK_CHANNEL')
        self.slack_log_channel = os.getenv('SLACK_LOG_CHANNEL')
        self.slack_error_channel = os.getenv('SLACK_ERROR_CHANNEL') or os.getenv('SLACK_LOG_CHANNEL')
        self.slack_webhook_url = 'https://slack.com/api/chat.postMessage'


class SlackNotifier:
    def __init__(self, config):
        self.config = config
        
    def _send_notification(self, channel: str, message: str):
        payload = {
            'channel': channel,
            'text': message
        }
        headers = {
            'Authorization': f'Bearer {self.config.slack_token}',
            'Content-Type': 'application/json'
        }
        response = requests.post(self.config.slack_webhook_url, json=payload, headers=headers)
        print(response.text)
        
    def send_notification(self, message: str):
        self._send_notification(self.config.slack_channel, message)
        
    def send_log_notification(self, message: str):
        self._send_notification(self.config.slack_log_channel, message)

    def send_error_notification(self, message: str):
        """에러·추가 알림 (Rusaint 에러, 학생정보 매칭 실패, 졸업사정표 파싱 실패 등) → SLACK_ERROR_CHANNEL"""
        self._send_notification(self.config.slack_error_channel, message)


class TimeUtils:
    @staticmethod
    def get_kst_now() -> str:
        kst = pytz.timezone('Asia/Seoul')
        now_kst = datetime.now(pytz.utc).astimezone(kst)
        return now_kst.strftime('%Y-%m-%d %H:%M:%S')


config = Config()
notifier = SlackNotifier(config)
log_handlers = LogHandlers(config, notifier)
soongpt_handler = SoongptHandler(config, notifier)


def append_or_create_file(filename, content):
    with open(filename, 'a', encoding='utf-8') as f:
        f.write(content)


last_checked_line = dict()


def process_line_with_handlers(line, handlers):
    for prefix, handler_func in handlers.items():
        if prefix in line:
            try:
                handler_func(line)
                print(f"[Observer] Matched prefix -> Slack 전송 완료: {prefix[:50]}...")
            except Exception as e:
                error_message = f"🚨ALERT ERROR - {config.environment.upper()} SERVER🚨\nlogging: {line}\nError: {str(e)}"
                print(error_message)
                notifier.send_error_notification(error_message)
            break


def check(file_path):
    global last_checked_line

    with open(file_path, 'r', encoding='utf-8') as file:
        lines = file.readlines()
    if not lines:
        return
    # 파일을 처음 볼 때: 방금 append로 추가된 마지막 줄을 놓치지 않도록 (len-1)부터 읽음
    if file_path not in last_checked_line:
        last_checked_line[file_path] = max(0, len(lines) - 1)
    from_idx = last_checked_line[file_path]
    to_process = lines[from_idx:]

    for line in to_process:
        process_line_with_handlers(line, log_handlers.handlers)
        process_line_with_handlers(line, soongpt_handler.handlers)

    last_checked_line[file_path] += len(to_process)


class LogHandler(FileSystemEventHandler):
    @staticmethod
    def on_modified(event):
        if event.is_directory:
            return

        if event.src_path.endswith('.log'):
            check(event.src_path)


if __name__ == "__main__":
    # 프로젝트 루트에서 실행 시 logs/ 감시. OBSERVER_LOG_DIR 있으면 그 경로 사용.
    watch_log_dir = os.environ.get("OBSERVER_LOG_DIR", "").strip() or "logs/"
    watch_log_dir = os.path.abspath(os.path.expanduser(watch_log_dir))
    os.makedirs(watch_log_dir, exist_ok=True)
    event_handler = LogHandler()
    observer = Observer()
    observer.schedule(event_handler, watch_log_dir, recursive=True)
    observer.start()
    start_message = f"Observer started: {TimeUtils.get_kst_now()}"
    print(f"Watching: {watch_log_dir}")
    print(start_message)
    notifier.send_log_notification(start_message)
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()