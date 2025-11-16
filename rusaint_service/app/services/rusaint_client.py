from typing import Any


class RusaintClient:
    """
    rusaint 라이브러리를 래핑하는 클라이언트.

    - u-saint 토큰/세션을 활용해 시간표/수강 정보 등을 조회합니다.
    """

    def __init__(self) -> None:
        # TODO: rusaint.Client 초기화에 필요한 설정(캠퍼스, 학번 등) 주입
        # from rusaint import Client
        # self._client = Client(...)
        self._client = None

    async def fetch_courses(
        self,
        u_saint_token: str,
        student_id: str,
    ) -> list[dict[str, Any]]:
        """
        rusaint를 통해 원본 과목/시간표 데이터를 조회합니다.

        현재는 스켈레톤으로, 실제 rusaint 호출 로직은 추후 구현합니다.
        """
        # TODO:
        # 1) anyio.to_thread.run_sync(...)로 rusaint 동기 호출을 offload
        # 2) student_id, u_saint_token을 이용해 해당 학번의 시간표 조회
        return []
