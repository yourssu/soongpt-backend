package com.yourssu.soongpt.common.auth

/**
 * 현재 요청 스레드의 pseudonym을 보관하는 홀더.
 *
 * - **HTTP 요청**: [CurrentPseudonymFilter]가 요청 진입 시 한 번 세팅하고, 응답 후 clear.
 * - **비동기/스케줄러**: 호출하는 쪽에서 [set]으로 세팅한 뒤 서비스 호출. 사용 후 [clear] 권장.
 *
 * 컨트롤러→서비스로 pseudonym을 인자로 넘기지 않고, 서비스는 [get]으로만 조회하면 됨.
 */
object CurrentPseudonymHolder {
    private val holder = ThreadLocal.withInitial<String?> { null }

    fun set(pseudonym: String) {
        holder.set(pseudonym)
    }

    fun get(): String? = holder.get()

    fun clear() {
        holder.remove()
    }

    /**
     * pseudonym을 설정하고 블록을 실행한 후 자동으로 clear하는 헬퍼 함수.
     * ThreadLocal 메모리 누수를 방지하기 위해 finally 블록에서 항상 clear를 보장합니다.
     *
     * @param pseudonym 설정할 pseudonym 값
     * @param block 실행할 블록
     * @return 블록의 실행 결과
     *
     * @sample
     * ```
     * CurrentPseudonymHolder.withPseudonym(pseudonym) {
     *     untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
     * }
     * ```
     */
    inline fun <T> withPseudonym(pseudonym: String, block: () -> T): T {
        set(pseudonym)
        return try {
            block()
        } finally {
            clear()
        }
    }
}
