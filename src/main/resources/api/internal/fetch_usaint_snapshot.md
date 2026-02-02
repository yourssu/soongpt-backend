# fetchUsaintSnapshot (POST /api/usaint/snapshot/academic, /api/usaint/snapshot/graduation)

> **Internal API**
> 이 엔드포인트는 **WAS(Kotlin) ↔ rusaint-service(Python)** 간 통신에만 사용됩니다.
> 외부 클라이언트에는 노출되지 않습니다.
> u-saint로부터 **학적/성적 정보를 조회하여 “스냅샷 데이터”를 가져오는 역할**만 하며,
> SoongPT DB에 실제로 반영할지 여부는 상위 비즈니스 레이어에서 결정합니다.

---

## 요약

- **엔드포인트**: `POST /api/usaint/snapshot/academic`, `POST /api/usaint/snapshot/graduation`
- **인증**: WAS의 `InternalJwtIssuer`가 발급한 JWT를 `Authorization: Bearer {internal-jwt}` 로 전달. rusaint-service는 WAS와 동일 시크릿(`RUSAINT_INTERNAL_JWT_SECRET` / `INTERNAL_JWT_SECRET`)으로 검증.
- **요청**: Body에 `studentId`, `sToken` (WAS는 클라이언트로부터 받은 값을 그대로 전달).
- **응답**: rusaint-service가 `pseudonym`(HMAC-SHA256(studentId) → base64url, `PSEUDONYM_SECRET` 사용) 및 학적/성적/졸업 스냅샷 데이터 반환. WAS는 두 응답을 병합해 사용(academic → 0.5초 후 graduation 호출 권장).

**요청/응답 스키마, 에러 코드, 타임아웃, Rate Limiting 등 상세 스펙은 [rusaint_service/API_SPECIFICATION.md](../../../../../rusaint_service/API_SPECIFICATION.md) 를 참고하세요.**
