package com.connectapp.domain.shared.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `parse accepts comma or dot decimals and stores exact cents`() {
        assertEquals(Money(4500), Money.parse("45"))
        assertEquals(Money(4550), Money.parse("45,5"))
        assertEquals(Money(4550), Money.parse("45,50"))
        assertEquals(Money(4550), Money.parse("45.50"))
        assertEquals(Money(5), Money.parse("0,05"))
        assertEquals(Money(0), Money.parse("0"))
        assertEquals(Money(1999), Money.parse(" 19,99 "))
    }

    // 0.1 + 0.2 != 0.3 in floating point; in cents it is exact.
    @Test
    fun `sums are exact`() {
        assertEquals(Money(30), Money.parse("0,10")!! + Money.parse("0,20")!!)
    }

    @Test
    fun `parse rejects invalid input`() {
        listOf("", "  ", "-5", "+5", "45,505", "1.234,50", "abc", "45,", ",50", "4 5", "1e3")
            .forEach { assertNull(Money.parse(it), "must reject '$it'") }
    }

    @Test
    fun `format uses Spanish separators`() {
        assertEquals("0,05 €", Money(5).format())
        assertEquals("45,50 €", Money(4550).format())
        assertEquals("1.234,50 €", Money(123450).format())
        assertEquals("1.000.000,00 €", Money(100_000_000).format())
    }

    @Test
    fun `negative amounts and overflow are rejected`() {
        assertFailsWith<IllegalArgumentException> { Money(-1) }
        assertFailsWith<IllegalArgumentException> { Money(Long.MAX_VALUE) + Money(1) }
    }

    @Test
    fun `amounts compare by value`() {
        assertTrue(Money(100) < Money(101))
    }
}
