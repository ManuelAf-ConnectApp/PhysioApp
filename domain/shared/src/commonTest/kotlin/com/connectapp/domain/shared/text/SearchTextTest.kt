package com.connectapp.domain.shared.text

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchTextTest {

    @Test
    fun `accents and case are folded`() {
        assertEquals("inigo lopez nunez", SearchText.normalize("Íñigo López Núñez"))
        assertEquals("joao conceicao", SearchText.normalize("JOÃO Conceição"))
        assertEquals("muller strasse", SearchText.normalize("Müller Straße"))
        assertEquals("aeoe", SearchText.normalize("Æœ"))
    }

    @Test
    fun `surrounding and repeated whitespace is collapsed`() {
        assertEquals("aina garcia", SearchText.normalize("  Aina \t  García  "))
    }

    @Test
    fun `full name joins first and last name`() {
        assertEquals("aina garcia lopez", SearchText.fullName(" Aina ", "García López"))
    }

    @Test
    fun `characters outside the table are kept`() {
        assertEquals("o'neill-smith 3", SearchText.normalize("O'Neill-Smith 3"))
    }
}
