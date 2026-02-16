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
        self.GRADUATION_SUMMARY_ALERT_PREFIX = 'GraduationSummaryAlert'
        self.STUDENT_INFO_MAPPING_ALERT_PREFIX = 'StudentInfoMappingAlert'
        self.RUSAINT_SERVICE_ERROR_PREFIX = 'RusaintServiceError'

        # 핸들러 매핑 (에러 채널용 alert는 SLACK_ERROR_CHANNEL로 전달)
        self.handlers = {
            self.CREATE_CONTACT_PREFIX: self.create_contact,
            self.CREATE_TIMETABLE_PREFIX: self.create_timetable,
            self.GRADUATION_SUMMARY_ALERT_PREFIX: self.graduation_summary_alert,
            self.STUDENT_INFO_MAPPING_ALERT_PREFIX: self.student_info_mapping_alert,
            self.RUSAINT_SERVICE_ERROR_PREFIX: self.rusaint_service_error,
        }

    def _env_header(self):
        """dev/prod 구분용 첫 줄. ENVIRONMENT=dev|prod 기준으로 명확히 표시."""
        env = (self.config.environment or "").strip().upper()
        if env in ("DEV", "PROD"):
            return f"서버: *{env}*\n"
        return f"서버: *{env or '???'}*\n"

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
        """졸업사정표 파싱 실패 알림 → SLACK_ERROR_CHANNEL (1학년 제외, raw 데이터 있으면 graduationRequirements.requirement 코드블럭 포함)"""
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
        grade_raw = kv_dict.get('userGrade', 'N/A')
        missing = kv_dict.get('missingItems', 'N/A')
        raw_b64 = kv_dict.get('rawDataBase64', '')

        # 1학년(또는 학년 null/미표시)은 슬랙 알림 제외 (서버 로그에는 이미 찍힘)
        try:
            grade_num = int(grade_raw) if grade_raw and str(grade_raw).strip() not in ('', 'N/A') else None
        except (ValueError, TypeError):
            grade_num = None
        if grade_num == 1 or grade_num is None:
            return

        header = self._env_header()
        message = (
            f"{header}🟠 *졸업사정표 파싱 실패*\n"
            f"--------------------------\n"
            f"학과 : {department}\n"
            f"학년 : {grade_raw}학년\n"
            f"누락 항목 : {missing.replace(';', ', ')}\n"
            f"영향 : 이수현황 미표시(progress -2), 과목 추천은 정상 제공\n"
            f"조치 : graduation_summary_builder.py 파서 점검 필요\n"
            f"발생시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"
        )

        if raw_b64:
            try:
                raw_json = base64.b64decode(raw_b64).decode('utf-8')
                raw_pretty = json.dumps(json.loads(raw_json), ensure_ascii=False, indent=2)
                message += f"\n\n*graduationRequirements.requirement*\n```\n{raw_pretty}\n```"
                self.notifier.send_error_notification(message)
            except Exception as e:
                self.notifier.send_error_notification(f"{message}\n\n⚠️ raw 데이터 디코딩 실패: {e}")
        else:
            self.notifier.send_error_notification(message)

    def student_info_mapping_alert(self, line):
        """학생 정보 매칭 실패 알림 → SLACK_ERROR_CHANNEL"""
        data_part = line[line.find('&') + 1:].strip()
        try:
            data = json.loads(data_part)
        except json.JSONDecodeError:
            self.notifier.send_error_notification(f"{self._env_header()}🟡 *[학생 정보 매칭 실패]*\n파싱 오류: {data_part[:200]}")
            return
        prefix = data.get('studentIdPrefix', 'N/A')
        reason = data.get('failureReason', 'N/A')
        header = self._env_header()
        message = (
            f"{header}🟡 *[학생 정보 매칭 실패]* 사용자가 직접 입력해야 함\n"
            f"--------------------------\n"
            f"학번 : {prefix}****\n"
            f"실패 사유 : {reason}\n"
            f"발생시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"
        )
        self.notifier.send_error_notification(message)

    def rusaint_service_error(self, line):
        """Rusaint 서비스 에러/연결 실패 알림 → SLACK_ERROR_CHANNEL (validate-token 401 만료 제외, 26학번(2026) 제외)"""
        data_part = line[line.find('&') + 1:].strip()
        try:
            data = json.loads(data_part)
        except json.JSONDecodeError:
            self.notifier.send_error_notification(f"{self._env_header()}🔴 *[Rusaint 서비스 에러]*\n파싱 오류: {data_part[:200]}")
            return
        op = data.get('operation', 'N/A')
        status = data.get('statusCode')
        prefix = data.get('studentIdPrefix')
        # validate-token 401(토큰 만료)만 슬랙 알림 제외. 500/502/504 등 실제 장애는 알림 유지
        if op == 'validate-token' and status == 401:
            return
        # 26학번(2026 입학): 로깅은 WAS에서 그대로 하고, 슬랙 알림만 제외 (새내기 academic 파싱 실패 다수 예상)
        if prefix == '2026':
            return
        status_str = str(status) if status is not None else 'N/A'
        err = data.get('errorMessage', 'N/A')
        header = self._env_header()
        message = (
            f"{header}🔴 *[Rusaint 서비스 에러]*\n"
            f"--------------------------\n"
            f"Operation : {op}\n"
            f"Status Code : {status_str}\n"
            f"Error : {err}\n"
        )
        if prefix:
            message += f"학번 : {prefix}****\n"
        message += f"발생시간: {datetime.now().strftime('%Y/%m/%d %H:%M:%S')}"
        self.notifier.send_error_notification(message)
