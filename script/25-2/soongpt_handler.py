import base64
import json
from datetime import datetime

class SoongptHandler:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier

        # 숭피티 관련 로그 패턴
        self.CREATE_CONTACT_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - ContactCreated'
        self.CREATE_TIMETABLE_PREFIX = 'INFO com.yourssu.soongpt.common.infrastructure.notification.Notification - TimetableCreated'
        self.GRADUATION_SUMMARY_ALERT_PREFIX = 'WARN com.yourssu.soongpt.common.infrastructure.notification.Notification - GraduationSummaryAlert'

        # 핸들러 매핑
        self.handlers = {
            self.CREATE_CONTACT_PREFIX: self.create_contact,
            self.CREATE_TIMETABLE_PREFIX: self.create_timetable,
            self.GRADUATION_SUMMARY_ALERT_PREFIX: self.graduation_summary_alert,
        }

    def create_contact(self, line):
        id_part = line[line.find('&') + 1:].strip()
        message = f"""🚀 숭피티 사전 예약 등록 알림 🚀

📧 {id_part}번째 연락처가 등록되었어요!
⏰ 등록시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"""
        self.notifier.send_notification(message)

    def create_timetable(self, line):
        table_part = line[line.find('&') + 1:].strip()
        if table_part.startswith('{') and table_part.endswith('}'):
            table_part = table_part[1:-1]

        kv_dict = {}
        for pair in table_part.split(','):
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

    def graduation_summary_alert(self, line):
        """졸업사정표 파싱 실패 알림 → SLACK_LOG_CHANNEL (raw 데이터 있으면 코드블럭으로 포함)"""
        data_part = line[line.find('&') + 1:].strip()
        if data_part.startswith('{') and data_part.endswith('}'):
            data_part = data_part[1:-1]

        kv_dict = {}
        for pair in data_part.split(','):
            if ':' not in pair:
                continue
            key, val = pair.split(':', 1)
            kv_dict[key.strip()] = val.strip()

        department = kv_dict.get('departmentName', 'N/A')
        grade = kv_dict.get('userGrade', 'N/A')
        missing = kv_dict.get('missingItems', 'N/A')
        raw_b64 = kv_dict.get('rawDataBase64', '')

        message = (
            f"🟠 *졸업사정표 파싱 실패*\n"
            f"--------------------------\n"
            f"📚학과 : {department}\n"
            f"📖학년 : {grade}학년\n"
            f"❌누락 항목 : {missing.replace(';', ', ')}\n"
            f"💡영향 : 이수현황 미표시(progress -2), 과목 추천은 정상 제공\n"
            f"🔧조치 : graduation_summary_builder.py 파서 점검 필요\n"
            f"⏰ 발생시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"
        )

        if raw_b64:
            try:
                raw_json = base64.b64decode(raw_b64).decode('utf-8')
                raw_pretty = json.dumps(json.loads(raw_json), ensure_ascii=False, indent=2)
                message += f"\n\n*graduationRequirements.requirements (raw)*\n```\n{raw_pretty}\n```"
                self.notifier.send_log_notification(message)
            except Exception as e:
                self.notifier.send_log_notification(f"{message}\n\n⚠️ raw 데이터 디코딩 실패: {e}")
        else:
            self.notifier.send_log_notification(message)
