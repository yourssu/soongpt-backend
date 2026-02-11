# UntakenCourseCodeService 사용 가이드

> 미수강 과목코드를 이수구분(category)별로 조회하는 서비스.
> pseudonym·세션·ThreadLocal은 전부 내부에서 처리하므로, **category만 넘기면 된다.**

---

## Quick Start

서비스에서 `UntakenCourseCodeService`를 주입받고, category만 넣어 호출한다.

```kotlin
@Service
class MoruService(
    private val untakenCourseCodeService: UntakenCourseCodeService,
) {
    fun doSomething() {
        // 전공 계열 → List<Long> (10자리 과목코드)
        val majorBasic    = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_BASIC)
        val majorRequired = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
        val majorElective = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_ELECTIVE)

        // 교양 계열 → Map<분야명, List<Long>> (분야별 10자리 과목코드)
        val generalRequired = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_REQUIRED)
        val generalElective = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
    }
}
```

**이게 전부다.** pseudonym을 파라미터로 받거나 넘길 필요 없음.

---

## API Reference

### `getUntakenCourseCodes(category)` → `List<Long>`

전기 / 전필 / 전선용. 사용자가 아직 듣지 않은 과목의 10자리 코드를 flat list로 반환.

| category           | maxGrade    | 설명                              |
| ------------------ | ----------- | --------------------------------- |
| `MAJOR_BASIC`    | 사용자 학년 | 전기. 해당 학년까지 대상인 과목만 |
| `MAJOR_REQUIRED` | 사용자 학년 | 전필. 해당 학년까지 대상인 과목만 |
| `MAJOR_ELECTIVE` | 5 (전학년)  | 전선. 모든 학년 과목 포함         |

### `getUntakenCourseCodesByField(category)` → `Map<String, List<Long>>`

교필 / 교선용. **분야명 → 미수강 과목코드 리스트** 형태로 반환.
분야 매핑은 사용자 학번(`basicInfo.year % 100`) 기준으로 `FieldFinder`가 결정.

| category             | 이수 판정                                                      | 반환 예시                                                         |
| -------------------- | -------------------------------------------------------------- | ----------------------------------------------------------------- |
| `GENERAL_REQUIRED` | 분야 내**1개라도 이수** → 해당 분야 **빈 리스트** | `{"SW와AI": [], "글로벌시민의식": [2150012301, ...]}`           |
| `GENERAL_ELECTIVE` | 이수 과목만**개별 제외**                                 | `{"인간·언어": [2150034501, 2150034502], "사회와경제": [...]}` |

#### 교필 분야 (23학번 이후 기준 분야명)

| 분야               | 대상 학년 |
| ------------------ | --------- |
| 인문적상상력과소통 | 1학년     |
| 비판적사고와표현   | 1학년     |
| 인간과성서         | 1학년     |
| 한반도평화와통일   | 1학년     |
| 컴퓨팅적사고       | 1학년     |
| SW와AI             | 1학년     |
| 글로벌시민의식     | 2학년     |
| 글로벌소통과언어   | 2학년     |
| 창의적사고와혁신   | 3학년     |

> 22학번 이하는 학번에 맞는 분야명으로 반환된다 (예: `생활속의SW`, `글로벌소통` 등).

#### 교선 분야

학번별로 분야명이 다르다. 20학번이 보는 분야명과 23학번이 보는 분야명이 다름.
내부적으로 `FieldFinder.findFieldBySchoolId(course.field, schoolId)` 로 매핑.

---

## 컨트롤러에서 별도 작업이 필요한가?

### `CurrentPseudonymFilter`가 이미 타는 경우 (대부분)

**아무것도 안 해도 된다.** Filter가 쿠키 JWT에서 pseudonym을 자동으로 세팅해 준다.

```kotlin
@GetMapping("/my-endpoint")
fun myEndpoint(): ResponseEntity<Response<MyResult>> {
    // 컨트롤러에서 pseudonym 관련 코드 없음
    val result = myService.doSomething()
    return ResponseEntity.ok(Response(result = result))
}
```

### Filter가 안 타는 별도 경로일 때

컨트롤러에서 **한 번만** 세팅. 서비스로는 pseudonym을 넘기지 않는다.

```kotlin
@PostMapping("/my-endpoint")
fun myEndpoint(httpRequest: HttpServletRequest): ResponseEntity<Response<MyResult>> {
    val pseudonym = clientJwtProvider.extractPseudonymFromRequest(httpRequest)
        .getOrElse { throw UnauthorizedException(message = "재인증이 필요합니다.") }

    CurrentPseudonymHolder.set(pseudonym)
    try {
        val result = myService.doSomething()  // pseudonym 인자 없음
        return ResponseEntity.ok(Response(result = result))
    } finally {
        CurrentPseudonymHolder.clear()
    }
}
```

---

## 비동기 / 스케줄러에서 쓸 때

HTTP 요청 스레드가 아니면 Filter가 세팅하지 않으므로, pseudonym 인자를 직접 넘긴다.

```kotlin
// 방법: pseudonym 인자 오버로드 사용
val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED, pseudonym)
val fieldMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE, pseudonym)
```

## 요청 흐름

사용자 A가 GET /api/timetables/{id}/available-general-electives 호출
    ↓
[CurrentPseudonymFilter.doFilterInternal] 실행 (가장 먼저, @Order(0))
    ↓
쿠키에서 JWT 추출 → pseudonym 추출 → CurrentPseudonymHolder.set("A의 pseudonym")
    ↓
[컨트롤러] getAvailableGeneralElectives() 실행
    ↓
[서비스] timetableService.getAvailableGeneralElectives() 실행
    ↓
[UntakenCourseCodeService] getUntakenCourseCodes() 호출
    ↓
CurrentPseudonymHolder.get() → "A의 pseudonym" 반환 ✅
    ↓
[finally 블록] CurrentPseudonymHolder.clear() 실행

## Q&A 및 망가지는 조건(사실 내가 궁금했던 거임ㅋ ㅋ ㅋ ㅋ ㅋ)

Q. 사용자 A가 호출했을 때 A의 정보를 반드시 꺼낼 수 있는가?

A. 네, 가능합니다. ThreadLocal은 스레드별로 격리되며, HTTP 요청은 각각 별도 스레드에서 처리됩니다.

* 스레드 A: CurrentPseudonymHolder.set("userA") → ThreadLocal에 "userA" 저장
* 스레드 B: CurrentPseudonymHolder.set("userB") → ThreadLocal에 "userB" 저장 (독립적)
* 스레드 A에서 get() → "userA" 반환 (스레드 B의 값과 섞이지 않음)

주의사항: 쿠키가 없거나 JWT가 유효하지 않으면 extractPseudonymFromRequest가 실패하고 set()이 호출되지 않습니다.

* 이 경우 CurrentPseudonymHolder.get()은 null을 반환하고, UntakenCourseCodeService는 UnauthorizedException을 던집니다.
* Q.비동기 처리: 다른 스레드로 넘어가면 ThreadLocal이 비어있습니다.
* ```
  // 망가지는 경우
  @Async
  fun someAsyncMethod() {
      val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
      // → CurrentPseudonymHolder.get() = null (다른 스레드라서)
      // → UnauthorizedException 발생
  }

  // 해결 방법
  @Async
  fun someAsyncMethod(pseudonym: String) {
      val codes = untakenCourseCodeService.getUntakenCourseCodes(
          Category.MAJOR_REQUIRED,
          pseudonym  // 명시적으로 넘김
      )
  }
  ```

* 필터 실행 전 호출: 필터보다 먼저 실행되는 코드에서 호출하면 null입니다.

## 요약

| 상황                             | 하면 되는 것                                                               |
| -------------------------------- | -------------------------------------------------------------------------- |
| **일반 API** (Filter 활성) | `getUntakenCourseCodes(category)` 호출. 끝.                              |
| **Filter 안 타는 API**     | 컨트롤러에서 `CurrentPseudonymHolder.set()` 한 번 → 서비스는 category만 |
| **비동기 / 스케줄러**      | `getUntakenCourseCodes(category, pseudonym)` 오버로드 사용               |
