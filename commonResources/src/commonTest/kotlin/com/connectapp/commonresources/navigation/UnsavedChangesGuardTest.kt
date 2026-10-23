package com.connectapp.commonresources.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnsavedChangesGuardTest {

    @Test
    fun `the guard is dirty while any screen reports changes`() {
        val guard = UnsavedChangesGuard()
        val form = Any()
        val leaving = Any()

        guard.report(form, dirty = true)
        guard.report(leaving, dirty = true)
        guard.report(leaving, dirty = false)
        assertTrue(guard.hasUnsavedChanges)

        guard.report(form, dirty = false)
        assertFalse(guard.hasUnsavedChanges)
    }

    @Test
    fun `discarding drops every pending change`() {
        val guard = UnsavedChangesGuard()
        guard.report(Any(), dirty = true)

        guard.discardAll()

        assertFalse(guard.hasUnsavedChanges)
    }
}
