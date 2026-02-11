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
- **교필**: 분야명은 **23이후 기준으로 통일** (학번 무관). `schoolId = 23` 고정.
- **교선**: 사용자 학번(`basicInfo.year % 100`) 기준으로 `FieldFinder`가 결정.

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

> 교필은 학번에 관계없이 위 분야명으로 통일 반환된다. (20학번이어도 `SW와AI`, `글로벌시민의식`)

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

---

## 실제 데이터로 테스트하기 (dev 프로필)

### 전제 조건

- dev DB(MySQL)에 과목(course) + 대상(target) 데이터가 들어있어야 함
- local(H2)은 과목 데이터가 비어있어서 빈 결과만 나옴 → **dev 프로필로 띄울 것**

### Step 1: MockUsaintData 설정

`MockUsaintData.kt`의 `build()`를 테스트 시나리오에 맞게 수정한다.

```kotlin
// MockUsaintData.kt → build()
basicInfo = RusaintBasicInfoDto(
    year = 2023,           // 23학번. 교선 분야 매핑에 사용됨 (schoolId = 23)
    semester = 5,
    grade = 3,             // 3학년. 전기/전필은 3학년까지 과목만, 교필은 3학년 분야까지 조회
    department = "컴퓨터학부",  // ⚠️ 반드시 DB에 있는 학과명으로! 빈 문자열이면 에러남
),
takenCourses = listOf(
    // 이수한 과목의 8자리 코드. 이 과목들이 결과에서 제외됨.
    // 예: SW와AI 분야의 "SW기초" 과목 코드
    RusaintTakenCourseDto(year = 2024, semester = "1", subjectCodes = listOf("21501021")),
),
```

**주의: `department = ""`로 두면 `departmentReader.getByName("")`에서 에러난다. DB에 있는 학과명을 넣어야 한다.**

#### 테스트 시나리오 예시

| 시나리오 | 설정 |
|---|---|
| 20학번 1학년 (교선 분야명 차이 확인) | `year = 2020, grade = 1` |
| 23학번 3학년 (교필 3학년 분야까지) | `year = 2023, grade = 3` |
| 이수 과목 없음 (전체 미수강) | `takenCourses = emptyList()` |
| 특정 교필 분야 이수 완료 | `takenCourses`에 해당 분야 과목 8자리 코드 추가 |

### Step 2: dev 프로필로 서버 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Step 3: Mock 세션 생성

Swagger(`http://localhost:8080/swagger-ui`) 또는 curl로 mock 토큰 발급:

```bash
curl -X POST http://localhost:8080/api/dev/mock-user-token \
  -c cookies.txt
```

응답의 `soongpt_auth` 쿠키가 세팅된다. 이후 요청에 이 쿠키를 같이 보내면 된다.

### Step 4: UntakenCourseCodeService 결과 확인

**현재 이 서비스를 직접 호출하는 API 엔드포인트는 없다.**
테스트하려면 아래 중 하나를 선택:

#### 방법 A: 추천 API로 간접 확인

```bash
# 전필 추천 결과 (내부적으로 유사한 조회 로직 사용)
curl -b cookies.txt "http://localhost:8080/api/courses/recommend/all?category=MAJOR_REQUIRED"

# 교필 추천 결과
curl -b cookies.txt "http://localhost:8080/api/courses/recommend/all?category=GENERAL_REQUIRED"

# 교선 추천 결과
curl -b cookies.txt "http://localhost:8080/api/courses/recommend/all?category=GENERAL_ELECTIVE"
```

> 단, 추천 API는 `CourseRecommendApplicationService`를 거치며 결과를 가공하므로,
> `UntakenCourseCodeService`의 raw 출력(과목코드 리스트)과는 형태가 다르다.

#### 방법 B: dev 전용 테스트 엔드포인트 추가 (권장)

`SsoDevController`에 아래 엔드포인트를 추가하면 raw 결과를 바로 볼 수 있다:

```kotlin
// SsoDevController에 추가
@GetMapping("/untaken-codes")
fun getUntakenCodes(
    @RequestParam category: Category,
): ResponseEntity<Response<Any>> {
    val result = when (category) {
        Category.GENERAL_REQUIRED, Category.GENERAL_ELECTIVE ->
            untakenCourseCodeService.getUntakenCourseCodesByField(category)
        else ->
            untakenCourseCodeService.getUntakenCourseCodes(category)
    }
    return ResponseEntity.ok(Response(result = result))
}
```

그러면 이렇게 호출:

```bash
# 전기 미수강 과목코드 (List<Long>)
curl -b cookies.txt "http://localhost:8080/api/dev/untaken-codes?category=MAJOR_BASIC"

# 전필 미수강 과목코드 (List<Long>)
curl -b cookies.txt "http://localhost:8080/api/dev/untaken-codes?category=MAJOR_REQUIRED"

# 전선 미수강 과목코드 (List<Long>)
curl -b cookies.txt "http://localhost:8080/api/dev/untaken-codes?category=MAJOR_ELECTIVE"

# 교필 미수강 과목코드 (Map<분야명, List<Long>>)
curl -b cookies.txt "http://localhost:8080/api/dev/untaken-codes?category=GENERAL_REQUIRED"

# 교선 미수강 과목코드 (Map<분야명, List<Long>>)
curl -b cookies.txt "http://localhost:8080/api/dev/untaken-codes?category=GENERAL_ELECTIVE"
```

### Step 5: 결과 검증 체크리스트

| 확인 항목 | 기대 결과 |
|---|---|
| 전기/전필: 이수한 과목 baseCode가 결과에 없는가 | `takenCourses`에 넣은 코드의 baseCode(8자리)와 일치하는 과목 제외 |
| 전선: 전학년 과목이 나오는가 | grade에 관계없이 1~5학년 대상 과목 포함 |
| 교필: 분야명이 23이후 기준인가 | 20학번이어도 `SW와AI`, `글로벌시민의식` 등으로 표시 |
| 교필: 이수 분야가 빈 리스트인가 | `takenCourses`에 넣은 과목의 분야 → `[]` |
| 교필: 미이수 분야에 과목코드가 있는가 | 해당 분야 과목 전부 리스트에 포함 |
| 교선: 분야명이 학번 기준인가 | 20학번이면 20학번 분야명, 23학번이면 23학번 분야명 |
| 교선: 이수 과목만 제외되었는가 | 같은 분야의 다른 과목은 남아있어야 함 |
| 빈 department일 때 에러가 나는가 | `department = ""`이면 예외 발생 (정상 동작) |

---

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
