package com.yourssu.soongpt.common.validation

import com.yourssu.soongpt.common.util.SchoolIdRange
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SchoolIdValidator::class])
annotation class ValidSchoolId(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class SchoolIdValidator : ConstraintValidator<ValidSchoolId, Int?> {
    override fun isValid(value: Int?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        val isValid = SchoolIdRange.isValid(value)

        if (!isValid) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(SchoolIdRange.getValidationMessage())
                .addConstraintViolation()
        }

        return isValid
    }
}
