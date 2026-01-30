"""
RusaintService 관련 테스트.
"""

import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from app.services.rusaint_service import RusaintService
import asyncio


@pytest.mark.asyncio
async def test_fetch_usaint_snapshot_with_invalid_token():
    """유효하지 않은 SSO 토큰 처리 테스트"""
    service = RusaintService()
    
    with pytest.raises(ValueError) as exc_info:
        await service.fetch_usaint_snapshot(
            student_id="20231234",
            s_token="invalid-token",
        )
    
    assert "SSO 토큰" in str(exc_info.value) or "유효하지 않" in str(exc_info.value)


@pytest.mark.asyncio
async def test_session_cleanup_on_error():
    """에러 발생 시 세션 정리 테스트"""
    service = RusaintService()
    
    # Mock 세션 생성
    mock_session = MagicMock()
    mock_session.close = AsyncMock()
    
    with patch.object(service, '_create_session', return_value=mock_session):
        with patch.object(service, '_get_graduation_app', side_effect=Exception("Test error")):
            with pytest.raises(Exception):
                await service.fetch_usaint_snapshot(
                    student_id="20231234",
                    s_token="test-token",
                )
            
            # 세션이 닫혔는지 확인
            mock_session.close.assert_called_once()


@pytest.mark.asyncio
async def test_create_session_timeout():
    """세션 생성 타임아웃 테스트"""
    service = RusaintService()
    
    async def slow_session_builder(*args, **kwargs):
        await asyncio.sleep(100)  # 타임아웃보다 긴 시간
        return MagicMock()
    
    with patch('rusaint.USaintSessionBuilder') as mock_builder_class:
        mock_builder = MagicMock()
        mock_builder.with_token = slow_session_builder
        mock_builder_class.return_value = mock_builder
        
        with pytest.raises((asyncio.TimeoutError, ValueError)):
            await service._create_session("20231234", "test-token")


def test_semester_type_mapping():
    """학기 타입 매핑 상수 테스트"""
    import rusaint
    
    assert RusaintService.SEMESTER_TYPE_MAP[rusaint.SemesterType.ONE] == "1"
    assert RusaintService.SEMESTER_TYPE_MAP[rusaint.SemesterType.TWO] == "2"
    assert RusaintService.SEMESTER_TYPE_MAP[rusaint.SemesterType.SUMMER] == "SUMMER"
    assert RusaintService.SEMESTER_TYPE_MAP[rusaint.SemesterType.WINTER] == "WINTER"
