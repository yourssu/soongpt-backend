from datetime import datetime

class SoongptHandler:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier
        
        # 숭피티 관련 로그 패턴
        self.CREATE_CONTACT_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - ContactCreated'
        self.CREATE_TIMETABLE_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - TimetableCreated'

        # 핸들러 매핑
        self.handlers = {
            self.CREATE_CONTACT_PREFIX: self.create_contact,
            self.CREATE_TIMETABLE_PREFIX: self.create_timetable
        }
    
    def create_contact(self, line):
        id_part = line[line.find('&') + 1:].strip()
        message = f"""🚀 숭피티 사전 예약 등록 알림 🚀

📧 {id_part}번째 연락처가 등록되었어요!
⏰ 등록시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"""
        self.notifier.send_notification(message)

    def create_timetable(self, line):
        id_part = line[line.find('&') + 1:].strip()
        if id_part.startswith('{') and id_part.endswith('}'):
            id_part = id_part[1:-1]

        kv_dict = {}
        for pair in id_part.split(','):
            if ':' not in pair:
                continue
            key, val = pair.split(':', 1)

            # 따옴표·공백 제거
            key = key.strip().strip('"').strip("'")
            val = val.strip().strip('"').strip("'")
            kv_dict[key] = val

        student_id = kv_dict.get('schoolId', 'N/A')
        department  = kv_dict.get('departmentName', 'N/A')
        total_cnt   = kv_dict.get('times', 'N/A')

        message = (
            f"""🎉 시간표 생성 알림 🎉
--------------------------
👤학번 : {student_id}
📚학과 : {department}
👥누적 시간표 생성 개수: {total_cnt}회
⏰ 등록시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"""
        )
        self.notifier.send_notification(message)