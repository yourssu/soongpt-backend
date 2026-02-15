"""
졸업사정표 요약 관련 스키마.

name 기반 분류 결과를 담는 스키마입니다.
매칭되지 않은 항목은 null. 비즈니스 로직(센티널 -2/-1/0,0,true)은 WAS에서 처리.
"""

from typing import Optional

from pydantic import BaseModel, Field


class CreditSummaryItem(BaseModel):
    """학점 기반 졸업 요건 요약 항목"""

    required: int = Field(..., description="필요 학점")
    completed: int = Field(..., description="이수 학점")
    satisfied: bool = Field(..., description="충족 여부")


class ChapelSummaryItem(BaseModel):
    """채플 요건 요약 (학점 없이 충족 여부만)"""

    satisfied: bool = Field(..., description="충족 여부")


class GraduationSummary(BaseModel):
    """졸업사정표 핵심 요약 (name 기반 분류 결과). 매칭되지 않은 항목은 null."""

    generalRequired: Optional[CreditSummaryItem] = Field(
        None, description="교양필수"
    )
    generalElective: Optional[CreditSummaryItem] = Field(
        None, description="교양선택"
    )
    majorFoundation: Optional[CreditSummaryItem] = Field(
        None, description="전공기초"
    )
    majorRequired: Optional[CreditSummaryItem] = Field(
        None, description="전공필수"
    )
    majorElective: Optional[CreditSummaryItem] = Field(
        None, description="전공선택"
    )
    majorRequiredElectiveCombined: bool = Field(
        False,
        description="전필·전선이 복합필드인 경우 True. majorRequired=majorElective 동일값, WAS는 warning으로 전달.",
    )
    minor: Optional[CreditSummaryItem] = Field(
        None, description="부전공"
    )
    doubleMajorRequired: Optional[CreditSummaryItem] = Field(
        None, description="복수전공필수"
    )
    doubleMajorElective: Optional[CreditSummaryItem] = Field(
        None, description="복수전공선택"
    )
    christianCourses: Optional[CreditSummaryItem] = Field(
        None, description="기독교과목"
    )
    chapel: Optional[ChapelSummaryItem] = Field(
        None, description="채플"
    )
