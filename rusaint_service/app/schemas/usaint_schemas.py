"""
유세인트 데이터 스냅샷 관련 스키마.

WAS(Kotlin) <-> rusaint-service(Python) 간 통신에 사용됩니다.
"""

from pydantic import BaseModel, Field
from typing import Optional


# ============================================================
# Request Schemas
# ============================================================


class UsaintSnapshotRequest(BaseModel):
    """유세인트 데이터 스냅샷 요청"""

    studentId: str = Field(..., pattern=r"^\d{8}$", description="학번 (8자리 숫자)")
    sToken: str = Field(..., min_length=1, description="SSO 토큰")


# ============================================================
# Response Schemas
# ============================================================


class TakenCourse(BaseModel):
    """학기별 수강 과목 정보"""

    year: int = Field(..., description="기준 학년도 (예: 2024)")
    semester: str = Field(..., description="학기 ('1': 1학기, '2': 2학기, 'SUMMER': 여름학기, 'WINTER': 겨울학기)")
    subjectCodes: list[str] = Field(default_factory=list, description="해당 학기 수강 과목 코드 리스트")


class LowGradeSubjectCodes(BaseModel):
    """저성적 과목 코드 목록 (C/D 및 F 성적)"""

    passLow: list[str] = Field(
        default_factory=list,
        description="C/D 성적(통과 저성적) 과목 코드 리스트 (이수구분 없이 과목 코드만)",
    )
    fail: list[str] = Field(
        default_factory=list,
        description="F 성적(재수강 필요) 과목 코드 리스트 (이수구분 없이 과목 코드만)",
    )


class Flags(BaseModel):
    """복수전공/부전공 및 교직 이수 정보"""

    doubleMajorDepartment: Optional[str] = Field(None, description="복수전공 학과명")
    minorDepartment: Optional[str] = Field(None, description="부전공 학과명")
    teaching: bool = Field(False, description="교직 이수 여부")


class BasicInfo(BaseModel):
    """기본 학적 정보"""

    year: int = Field(..., description="기준 연도 (예: 2025)")
    grade: int = Field(..., ge=1, le=4, description="학년 (1~4)")
    semester: int = Field(..., ge=1, le=8, description="재학 누적 학기 (1~8)")
    department: str = Field(..., description="주전공 학과명")


class GraduationRequirementItem(BaseModel):
    """개별 졸업 요건 항목"""

    name: str = Field(..., description="졸업요건 이름 (예: '학부-교양필수 19')")
    requirement: Optional[int] = Field(None, description="기준 학점 (None일 경우 요구사항 없음)")
    calculation: Optional[float] = Field(None, description="현재 이수 학점")
    difference: Optional[float] = Field(None, description="차이 (이수-기준, 음수면 부족)")
    result: bool = Field(..., description="충족 여부 (true: 충족, false: 미충족)")
    category: str = Field(..., description="이수구분 (예: '전공필수', '교양선택')")


class GraduationRequirements(BaseModel):
    """졸업 요건 전체 목록 (raw 데이터)"""

    requirements: list[GraduationRequirementItem] = Field(
        default_factory=list,
        description="개별 졸업 요건 항목 목록"
    )


class UsaintSnapshotResponse(BaseModel):
    """유세인트 데이터 스냅샷 응답"""

    takenCourses: list[TakenCourse] = Field(default_factory=list, description="학기별 수강 과목 코드 목록")
    lowGradeSubjectCodes: LowGradeSubjectCodes = Field(
        default_factory=LowGradeSubjectCodes,
        description="C/D 및 F 성적 과목 코드 목록",
    )
    flags: Flags = Field(default_factory=Flags, description="복수전공/부전공 및 교직 이수 정보")
    basicInfo: BasicInfo = Field(..., description="기본 학적 정보")
