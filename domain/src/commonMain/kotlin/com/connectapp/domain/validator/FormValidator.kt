package com.connectapp.domain.validator

import com.connectapp.domain.validator.`interface`.Validator
import com.connectapp.domain.validator.model.PatientValidationResult
import com.connectapp.domain.validator.model.ProfessionalValidationResult
import com.connectapp.domain.validator.model.ValidationError


class FormValidator : Validator {

    companion object {
        const val VALID_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE"
        const val DNI_REGEX =
            "^([0-9]{8}|[XYZ][0-9]{7})[TRWAGMYFPDXBNJZSQVHLCKE]$"

        const val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"

        const val PHONE_REGEX = "^[689][0-9]{8}$"

    }

    fun validatePatient(
        name: String,
        surname: String,
        email: String,
        phone: String,
        dni: String,
        birthDate: String
    ): PatientValidationResult {
        return PatientValidationResult(
            nameError = validateName(name),
            surnameError = validateSurname(surname),
            emailError = validateEmail(email),
            phoneError = validatePhone(phone),
            dniError = validateDni(dni),
            birthDateError = if (birthDate.isBlank()) ValidationError.FIELD_EMPTY else null
        )
    }

    fun validateProfessional(
        name: String,
        surname: String,
        email: String,
        phone: String,
        specialty: String,
        collegiateNumber: String
    ): ProfessionalValidationResult {
        return ProfessionalValidationResult(
            nameError = validateName(name),
            surnameError = validateSurname(surname),
            emailError = validateEmail(email),
            phoneError = validatePhone(phone),
            specialtyError = if (specialty.isBlank()) ValidationError.FIELD_EMPTY else null,
            collegiateNumberError = if (collegiateNumber.isBlank()) ValidationError.FIELD_EMPTY else null
        )
    }

    override fun validateName(name: String): ValidationError? = when {
        name.isBlank() -> ValidationError.FIELD_EMPTY
        name.length < 3 -> ValidationError.TOO_SHORT
        else -> null
    }

    override fun validateSurname(surname: String): ValidationError? = when {
        surname.isBlank() -> ValidationError.FIELD_EMPTY
        surname.length < 3 -> ValidationError.TOO_SHORT
        else -> null
    }

    override fun validateEmail(email: String): ValidationError? {
        return when {
            email.isBlank() -> ValidationError.FIELD_EMPTY
            !EMAIL_REGEX.toRegex().matches(email) -> ValidationError.INVALID_EMAIL
            else -> null
        }
    }

    override fun validatePhone(phone: String): ValidationError? {
        return when {
            phone.isBlank() -> ValidationError.FIELD_EMPTY
            !PHONE_REGEX.toRegex()
                .matches(phone) -> ValidationError.INVALID_PHONE

            else -> null
        }
    }

    override fun validateDni(dni: String): ValidationError? {
        return when {
            dni.isBlank() -> ValidationError.FIELD_EMPTY
            !DNI_REGEX.toRegex(RegexOption.IGNORE_CASE).matches(dni) -> ValidationError.INVALID_DNI
            !isValidDni(dni) -> ValidationError.INVALID_DNI_LETTER
            else -> null
        }
    }

    override fun validatePassword(password: String): ValidationError? {
        return when {
            password.isBlank() -> ValidationError.FIELD_EMPTY
            password.length < 6 -> ValidationError.INVALID_PASSWORD
            else -> null
        }
    }

    override fun isValidDni(dni: String): Boolean {
        if (dni.length != 9) return false

        val firstChar = dni[0].uppercaseChar()
        val normalizedDni = when (firstChar) {
            'X' -> "0" + dni.substring(1, 8)
            'Y' -> "1" + dni.substring(1, 8)
            'Z' -> "2" + dni.substring(1, 8)
            in '0'..'9' -> dni.substring(0, 8)
            else -> return false
        }

        val number = normalizedDni.toIntOrNull() ?: return false
        val letter = dni.last().uppercaseChar()

        return VALID_LETTERS[number % 23] == letter
    }
}
