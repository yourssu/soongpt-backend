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
        load_dotenv()
        self.environment = os.getenv('ENVIRONMENT')
        self.slack_token = os.getenv('SLACK_TOKEN')
        self.slack_channel = os.getenv('SLACK_CHANNEL')
        self.slack_log_channel = os.getenv('SLACK_LOG_CHANNEL')
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


def check(file_path):
    global last_checked_line

    with open(file_path, 'r') as file:
        lines = file.readlines()
        if file_path not in last_checked_line:
            last_checked_line[file_path] = len(lines)
        lines = lines[last_checked_line.get(file_path):]

    for line in lines:
        for prefix, handler_func in log_handlers.handlers.items():
            if prefix in line:
                try:
                    handler_func(line)
                except Exception as e:
                    error_message = f"🚨ALERT ERROR - {config.environment.upper()} SERVER🚨\nlogging: {line}\nError: {str(e)}"
                    print(error_message)
                    notifier.send_log_notification(error_message)
                break

    last_checked_line[file_path] += len(lines)


class LogHandler(FileSystemEventHandler):
    @staticmethod
    def on_modified(event):
        if event.is_directory:
            return

        if event.src_path.endswith('.log'):
            check(event.src_path)


if __name__ == "__main__":
    path = "logs/"
    event_handler = LogHandler()
    observer = Observer()
    observer.schedule(event_handler, path, recursive=True)
    observer.start()
    start_message = f"Observer started: {TimeUtils.get_kst_now()}"
    print(start_message)
    notifier.send_log_notification(start_message)
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()