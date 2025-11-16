from fastapi import Header


async def verify_internal_jwt(authorization: str | None = Header(default=None)) -> None:
    """
    WAS(Kotlin)에서 넘어오는 내부 JWT를 검증하기 위한 훅.

    현재는 스켈레톤으로, 실제 검증 로직(TLS+공유 시크릿/공개키 기반 서명 검증 등)은
    추후 구현합니다.
    """
    # TODO: Authorization 헤더(Bearer 토큰)를 파싱하고, 서명/만료 등을 검증
    # 검증 실패 시 HTTPException(status_code=401) 등을 발생시키도록 구현
    return
