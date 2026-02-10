"""
rusaint-service 커스텀 예외.

에러 원인을 명확히 구분하여 라우터에서 적절한 HTTP 상태코드를 반환할 수 있도록 합니다.
"""


class SSOTokenError(Exception):
    """SSO 토큰 만료 또는 무효 (숭실대 서버가 거부)."""
    pass


class RusaintConnectionError(Exception):
    """숭실대 유세인트 서버 연결 실패 (네트워크, DNS, SSL 등)."""
    pass


class RusaintTimeoutError(Exception):
    """숭실대 유세인트 서버 응답 시간 초과."""
    pass


class RusaintInternalError(Exception):
    """rusaint 라이브러리 내부 오류."""
    pass
