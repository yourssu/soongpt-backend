from pydantic import BaseModel


class TimetableRequest(BaseModel):
    """
    WAS(Kotlin) -> Python 서버로 넘어오는 payload.
    """

    pseudonym: str
    studentId: str
    sToken: str


class FilteredCourse(BaseModel):
    """
    rusaint 원본 데이터에서 필터링한 과목 정보.
    - 성적 등 민감 정보는 포함하지 않습니다.
    """

    subjectCode: str
    # TODO: 필요 시 과목명, 학점 등 추가 필드 정의


class TimetableResponse(BaseModel):
    """
    Python 서버 -> WAS(Kotlin)으로 반환하는 최종 응답.
    """

    pseudonym: str
    courses: list[FilteredCourse]
