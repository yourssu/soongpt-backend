from datetime import datetime


def append_or_create_file(filename, content):
    with open(filename, 'a', encoding='utf-8') as f:
        f.write(content)


class SoongptHandler:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier
        
        # 숭피티 관련 로그 패턴
        self.CREATE_CONTACT_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - ContactCreated'
        
        # 핸들러 매핑
        self.handlers = {
            self.CREATE_CONTACT_PREFIX: self.create_contact,
        }
    
    def create_contact(self, line):
        id_part = line[line.find('&') + 1:].strip()
        message = f"""🚀 **숭피티 사전 예약 등록 알림** 🚀
                    📧 {id_part}번째 연락처가 등록되었어요!
                    ⏰ 등록시간: {datetime.now().strftime('%m/%d %H:%M')}"""
        append_or_create_file("/home/ubuntu/soongpt-api/notification.txt", message)
        self.notifier.send_notification(message)
