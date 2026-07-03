package com.connectapp.domain.validator.`interface`

import com.connectapp.domain.validator.model.ValidationError

interface Validator {

    fun validateName(name: String): ValidationError?

    fun validateSurname(surname: String): ValidationError?

    fun validateEmail(email: String): ValidationError?

    fun validatePhone(phone: String): ValidationError?

    fun validateDni(dni: String): ValidationError?

    fun validatePassword(password: String): ValidationError?

    fun isValidDni(dni: String): Boolean
}
