package com.connectapp.commonresources.format

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** `dd/MM/yyyy HH:mm` in the device's time zone (signatures, reports…). */
@OptIn(ExperimentalTime::class)
fun formatDateTime(epochMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
    fun Int.twoDigits() = toString().padStart(2, '0')
    return "${time.day.twoDigits()}/${(time.month.ordinal + 1).twoDigits()}/${time.year} " +
        "${time.hour.twoDigits()}:${time.minute.twoDigits()}"
}
