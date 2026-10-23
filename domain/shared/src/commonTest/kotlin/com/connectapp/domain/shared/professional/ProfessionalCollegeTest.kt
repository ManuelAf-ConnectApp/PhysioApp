package com.connectapp.domain.shared.professional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfessionalCollegeTest {

    // Exactly the 17 regional colleges, no Ceuta or Melilla entry.
    @Test
    fun `the catalogue has the 17 regional colleges`() {
        assertEquals(17, ProfessionalCollege.entries.size)
        assertEquals(17, ProfessionalCollege.entries.map { it.displayName }.toSet().size)
    }

    @Test
    fun `stored ids map back to their college and unknown ids are rejected`() {
        ProfessionalCollege.entries.forEach { assertEquals(it, ProfessionalCollege.fromId(it.name)) }
        assertNull(ProfessionalCollege.fromId("CEUTA"))
        assertNull(ProfessionalCollege.fromId("andalucia"))
    }
}
