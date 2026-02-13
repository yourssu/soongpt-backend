"""
졸업사정표 요약 관련 스키마.

name 기반 분류 결과를 담는 스키마입니다.
"""

from typing import Optional

from pydantic import BaseModel, Field


class CreditSummaryItem(BaseModel):
    """학점 기반 졸업 요건 요약 항목"""

    required: int = Field(0, description="필요 학점")
    completed: int = Field(0, description="이수 학점")
    satisfied: bool = Field(True, description="충족 여부")


class ChapelSummaryItem(BaseModel):
    """채플 요건 요약 (학점 없이 충족 여부만)"""

    satisfied: bool = Field(False, description="충족 여부")


class GraduationSummary(BaseModel):
    """졸업사정표 핵심 요약 (name 기반 분류 결과). 매칭되지 않은 항목은 null."""

    generalRequired: Optional[CreditSummaryItem] = Field(
        None,
        description="교양필수",
    )
    generalElective: Optional[CreditSummaryItem] = Field(
        None,
        description="교양선택",
    )
    majorFoundation: Optional[CreditSummaryItem] = Field(
        None,
        description="전공기초",
    )
    majorRequired: Optional[CreditSummaryItem] = Field(
        None,
        description="전공필수",
    )
    majorElective: Optional[CreditSummaryItem] = Field(
        None,
        description="전공선택",
    )
    minor: Optional[CreditSummaryItem] = Field(
        None,
        description="부전공",
    )
    doubleMajorRequired: Optional[CreditSummaryItem] = Field(
        None,
        description="복수전공필수",
    )
    doubleMajorElective: Optional[CreditSummaryItem] = Field(
        None,
        description="복수전공선택",
    )
    christianCourses: Optional[CreditSummaryItem] = Field(
        None,
        description="기독교과목",
    )
    chapel: Optional[ChapelSummaryItem] = Field(
        None,
        description="채플",
    )
