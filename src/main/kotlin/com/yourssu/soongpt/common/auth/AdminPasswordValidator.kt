package com.yourssu.soongpt.common.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AdminPasswordValidator(
    @Value("\${admin.password}")
    private val adminPassword: String
) {
    fun validate(password: String?): Boolean {
        if (password.isNullOrBlank() || adminPassword.isBlank()) {
            return false
        }
        return password == adminPassword
    }
}
