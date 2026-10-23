package com.connectapp.commonresources.format

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatTest {

    @Test
    fun `dates are shown day first with a 24-hour clock`() {
        // 2023-11-14T22:13:20Z
        assertEquals("14/11/2023 22:13", formatDateTime(1_700_000_000_000L, TimeZone.UTC))
        assertEquals("14/11/2023 23:13", formatDateTime(1_700_000_000_000L, TimeZone.of("Europe/Madrid")))
    }
}
