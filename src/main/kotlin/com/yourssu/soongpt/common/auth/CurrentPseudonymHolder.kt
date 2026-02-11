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
}
