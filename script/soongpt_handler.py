from datetime import datetime

class SoongptHandler:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier
        
        # 숭피티 관련 로그 패턴
        self.CREATE_CONTACT_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - ContactCreated'
        self.CREATE_TIMETABLE_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - ContactCreated'

        # 핸들러 매핑
        self.handlers = {
            self.CREATE_CONTACT_PREFIX: self.create_contact,
            self.CREATE_HANDLER_PREFIX: self.create_timetable
        }
    
    def create_contact(self, line):
        id_part = line[line.find('&') + 1:].strip()
        message = f"""🚀 숭피티 사전 예약 등록 알림 🚀

📧 {id_part}번째 연락처가 등록되었어요!
⏰ 등록시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"""
        self.notifier.send_notification(message)

    def create_timetable(self, line):
        id_part = line[line.find('&') + 1:].strip()
        message = f"""🕰️ 숭피티 시간표 등록 알림 🕰️

📧 {id_part}번째 시간표가 등록되었어요!
⏰ 등록시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"""
        self.notifier.send_notification(message)