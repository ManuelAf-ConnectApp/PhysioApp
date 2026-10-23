package com.connectapp.domain.shared.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpanishIdValidatorTest {

    @Test
    fun `valid DNI-style NIFs are accepted`() {
        listOf("12345678Z", "00000000T", "87654321X", "12345678z", " 12345678-Z ", "12.345.678-Z")
            .forEach { assertTrue(SpanishIdValidator.isValid(it), "must accept '$it'") }
    }

    @Test
    fun `valid NIEs are accepted`() {
        listOf("X1234567L", "Y1234567X", "Z7654321H", "x1234567l")
            .forEach { assertTrue(SpanishIdValidator.isValid(it), "must accept '$it'") }
    }

    @Test
    fun `wrong control letters and malformed values are rejected`() {
        listOf("12345678A", "X1234567A", "1234567Z", "123456789Z", "W1234567L", "12345678", "ABCDEFGHZ", "", "K1234567L")
            .forEach { assertFalse(SpanishIdValidator.isValid(it), "must reject '$it'") }
    }

    @Test
    fun `normalize produces the canonical stored form`() {
        assertEquals("12345678Z", SpanishIdValidator.normalize(" 12.345.678-z "))
    }
}
