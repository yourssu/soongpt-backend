package com.yourssu.soongpt.common.validation

import com.yourssu.soongpt.common.util.SchoolIdRange
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [StudentIdValidator::class])
annotation class ValidStudentId(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class StudentIdValidator : ConstraintValidator<ValidStudentId, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        if (!value.matches(FORMAT_REGEX)) {
            setMessage(context, "학번 형식이 올바르지 않습니다. (8자리 숫자, 20으로 시작)")
            return false
        }

        val schoolId = value.substring(2, 4).toInt()
        if (!SchoolIdRange.isValid(schoolId)) {
            setMessage(context, SchoolIdRange.getValidationMessage())
            return false
        }

        return true
    }

    private fun setMessage(context: ConstraintValidatorContext, message: String) {
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation()
    }

    companion object {
        private val FORMAT_REGEX = Regex("^20\\d{6}$")
    }
}
