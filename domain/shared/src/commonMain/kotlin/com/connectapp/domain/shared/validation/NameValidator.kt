package com.connectapp.domain.shared.validation

/**
 * First name and last names of accounts, professionals and patients must not be blank once
 * leading/trailing whitespace is removed. D9 (confirmed 2026-10-20) replaced the original
 * "longer than 3 characters" rule of the original requirements, which rejected real names like "Ana",
 * "Luz" or "Li".
 */
object NameValidator {
    fun isValid(value: String): Boolean = value.isNotBlank()
}
