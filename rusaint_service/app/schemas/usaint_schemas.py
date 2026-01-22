"""
유세인트 데이터 스냅샷 관련 스키마.

WAS(Kotlin) <-> rusaint-service(Python) 간 통신에 사용됩니다.
"""

from pydantic import BaseModel, Field
from typing import Optional


# ============================================================
# Request Schemas
# ============================================================


class UsaintSyncRequest(BaseModel):
    """유세인트 데이터 동기화 요청"""

    studentId: str = Field(..., min_length=8, max_length=8, description="학번 (8자리)")
    sToken: str = Field(..., min_length=1, description="SSO 토큰")


# ============================================================
# Response Schemas
# ============================================================


class TakenCourse(BaseModel):
    """학기별 수강 과목 정보"""

    year: int = Field(..., description="기준 학년도 (예: 2024)")
    semester: int = Field(..., ge=1, le=2, description="학기 (1: 1학기, 2: 2학기)")
    subjectCodes: list[str] = Field(default_factory=list, description="해당 학기 수강 과목 코드 리스트")


class GradeBandSubjectCodes(BaseModel):
    """성적 구간별 과목 코드 목록 (전필/전선/교필/교선으로 구분)"""

    majorRequired: list[str] = Field(default_factory=list, description="전공필수 과목 코드 목록")
    majorElective: list[str] = Field(default_factory=list, description="전공선택 과목 코드 목록")
    generalRequired: list[str] = Field(default_factory=list, description="교양필수 과목 코드 목록")
    generalElective: list[str] = Field(default_factory=list, description="교양선택 과목 코드 목록")


class LowGradeSubjectCodes(BaseModel):
    """저성적 과목 코드 목록 (C/D 및 F 성적)"""

    passLow: GradeBandSubjectCodes = Field(
        default_factory=GradeBandSubjectCodes,
        description="C/D 성적(통과 저성적) 과목 코드 목록",
    )
    fail: GradeBandSubjectCodes = Field(
        default_factory=GradeBandSubjectCodes,
        description="F 성적(재수강 필요) 과목 코드 목록",
    )


class Flags(BaseModel):
    """복수전공/부전공 및 교직 이수 정보"""

    doubleMajorDepartment: Optional[str] = Field(None, description="복수전공 학과명")
    minorDepartment: Optional[str] = Field(None, description="부전공 학과명")
    teaching: bool = Field(False, description="교직 이수 여부")


class AvailableCredits(BaseModel):
    """직전 성적 및 최대 신청 가능 학점 정보"""

    previousGpa: float = Field(..., description="직전 학기 평점")
    carriedOverCredits: int = Field(..., description="이월 학점")
    maxAvailableCredits: int = Field(..., description="이번 학기 최대 신청 가능 학점")


class BasicInfo(BaseModel):
    """기본 학적 정보"""

    year: int = Field(..., description="기준 연도 (예: 2025)")
    grade: int = Field(..., ge=1, le=4, description="학년 (1~4)")
    semester: int = Field(..., ge=1, le=8, description="재학 누적 학기 (1~8)")
    department: str = Field(..., description="주전공 학과명")


class RemainingCredits(BaseModel):
    """졸업까지 남은 이수 학점 정보"""

    majorRequired: int = Field(..., description="남은 전공필수 학점")
    majorElective: int = Field(..., description="남은 전공선택 학점")
    generalRequired: int = Field(..., description="남은 교양필수 학점")
    generalElective: int = Field(..., description="남은 교양선택 학점")


class UsaintSnapshotResponse(BaseModel):
    """유세인트 데이터 스냅샷 응답"""

    takenCourses: list[TakenCourse] = Field(default_factory=list, description="학기별 수강 과목 코드 목록")
    lowGradeSubjectCodes: LowGradeSubjectCodes = Field(
        default_factory=LowGradeSubjectCodes,
        description="C/D 및 F 성적 과목 코드 목록",
    )
    flags: Flags = Field(default_factory=Flags, description="복수전공/부전공 및 교직 이수 정보")
    availableCredits: AvailableCredits = Field(..., description="직전 성적 및 최대 신청 가능 학점 정보")
    basicInfo: BasicInfo = Field(..., description="기본 학적 정보")
    remainingCredits: RemainingCredits = Field(..., description="졸업까지 남은 이수 학점 정보")
