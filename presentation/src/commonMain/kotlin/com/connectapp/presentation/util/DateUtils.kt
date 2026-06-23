package com.connectapp.presentation.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant


fun formatDate(millis: Long): String {
    // In a real KMP project, you should use kotlinx-datetime:
    val instant = Instant.fromEpochMilliseconds(millis)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.day}/${date.month.number}/${date.year}"

}