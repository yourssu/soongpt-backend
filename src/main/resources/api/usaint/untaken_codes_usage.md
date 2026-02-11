# UntakenCourseCodeService 사용법 (모루 연동 예시)

---

## 1. 모루가 자기 API 비즈니스 로직 안에서 쓸 때 (같은 코드베이스)

모루가 **자기가 만든 API**를 짜고, 그 **비즈니스 로직 안에서** 미수강 과목코드를 쓰는 경우입니다.
이때는 우리 Filter가 그 요청에 대해 이미 pseudonym을 세팅해 두지 **않을 수** 있으므로, **컨트롤러 진입 시 한 번만 뽑아서 Holder에 세팅**하면 됩니다.
그러면 **서비스까지 pseudonym을 인자로 끌고 올 필요 없습니다.**

### 컨트롤러: request에서 한 번 뽑아서 Holder 세팅

```kotlin
import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.handler.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/moru")
class MoruController(
    private val clientJwtProvider: ClientJwtProvider,
    private val moruService: MoruService,
) {
    @PostMapping("/timetable/generate")  // 예: 모루가 만든 시간표 생성 API
    fun generateTimetable(httpRequest: HttpServletRequest): ResponseEntity<Response<MoruResult>> {
        val pseudonym = clientJwtProvider.extractPseudonymFromRequest(httpRequest)
            .getOrElse { throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.") }

        // 여기서 한 번만 세팅. 서비스에는 pseudonym 안 넘김.
        CurrentPseudonymHolder.set(pseudonym)
        try {
            val result = moruService.generateTimetable()  // 인자 없음
            return ResponseEntity.ok().body(Response(result = result))
        } finally {
            CurrentPseudonymHolder.clear()
        }
    }
}
```

### MoruService: pseudonym 인자 없이 호출

```kotlin
@Service
class MoruService(
    private val untakenCourseCodeService: UntakenCourseCodeService,
    // ... 다른 의존성
) {
    /**
     * 컨트롤러에서 이미 CurrentPseudonymHolder.set(pseudonym) 해 둔 상태.
     * pseudonym을 인자로 받지 않음.
     */
    fun generateTimetable(): MoruResult {
        // 비즈니스 로직 중간에 미수강 과목코드 필요할 때 그냥 호출
        val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
        val fieldMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
        // ... 나머지 로직
        return MoruResult(/* ... */)
    }
}
```

**정리:**
- pseudonym은 **컨트롤러에서 한 번만** `clientJwtProvider.extractPseudonymFromRequest(request)` 로 뽑고,
- **`CurrentPseudonymHolder.set(pseudonym)`** 한 다음
- `moruService.xxx()` 처럼 **인자 없이** 서비스를 호출.
- 서비스는 **pseudonym을 파라미터로 받지 않고** `getUntakenCourseCodes(category)` 만 호출하면 됨. **끌고 올 필요 없음.**

---

## 2. 우리 Filter가 이미 타는 API에서 쓸 때

요청이 **CurrentPseudonymFilter**를 거치는 경로(우리 백엔드 공통 진입)라면, 컨트롤러에서 뽑거나 세팅할 필요 없이 **그냥 서비스만 호출**하면 됩니다.

```kotlin
@GetMapping("/untaken")
fun getUntaken(): ResponseEntity<Response<MoruUntakenResult>> {
    val result = moruService.getUntakenData()  // 인자 없음
    return ResponseEntity.ok().body(Response(result = result))
}
```

---

## 3. 필요한 카테고리만 골라서 호출

```kotlin
// 전공 계열 (전기/전필/전선) — List<Long> 반환
untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_BASIC)
untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_ELECTIVE)

// 교양 계열 (교필/교선) — Map<String, List<Long>> 반환 (분야별)
untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_REQUIRED)
untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
```

---

## 4. 비동기/스케줄러에서 쓸 때만

HTTP 요청이 아닌 스레드(비동기 작업, 스케줄러 등)에서는 Filter가 세팅하지 않으므로, 아래 둘 중 하나로 처리합니다.

**방법 A – 호출 시 pseudonym 인자로 넘기기**

```kotlin
val pseudonym = "이미 알고 있는 pseudonym"  // 예: 작업 큐에서 넘어온 값
val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED, pseudonym)
val fieldMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE, pseudonym)
```

**방법 B – 스레드 진입 시 Holder 세팅 후 호출**

```kotlin
CurrentPseudonymHolder.set(pseudonym)
try {
    val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
    val fieldMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
    // ...
} finally {
    CurrentPseudonymHolder.clear()
}
```

---

## 5. 정리

| 상황 | 어떻게 하면 되는지 |
|------|---------------------|
| **모루가 자기 API 비즈니스 로직 안에서 쓸 때** | 컨트롤러에서 `extractPseudonymFromRequest` → `CurrentPseudonymHolder.set(pseudonym)` 한 번만 하고, 서비스는 **인자 없이** `getUntakenCourseCodes(category)` 호출. **pseudonym을 서비스까지 끌고 올 필요 없음.** |
| **우리 Filter가 이미 타는 API** | 컨트롤러/서비스에서 그냥 `getUntakenCourseCodes(category)` 호출. |
| **비동기/스케줄러** | `getUntakenCourseCodes(category, pseudonym)` 로 넘기거나, 작업 시작 시 `CurrentPseudonymHolder.set(pseudonym)` 후 일반 호출. |
