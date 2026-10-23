package com.connectapp.domain.shared.money

/**
 * Exact monetary amount stored as an integer number of cents — never floating point
 * ("tratamiento monetario exacto"). Only euros are supported for now.
 */
data class Money(val cents: Long, val currency: Currency = Currency.EUR) : Comparable<Money> {

    enum class Currency(val code: String, val symbol: String) { EUR("EUR", "€") }

    init {
        require(cents >= 0) { "Money cannot be negative" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch" }
        require(cents <= Long.MAX_VALUE - other.cents) { "Money overflow" }
        return Money(cents + other.cents, currency)
    }

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Currency mismatch" }
        return cents.compareTo(other.cents)
    }

    /** Spanish formatting: `1.234,50 €`. */
    fun format(): String {
        val units = (cents / CENTS_PER_UNIT).toString()
        val grouped = units.reversed().chunked(3).joinToString(".").reversed()
        val fraction = (cents % CENTS_PER_UNIT).toString().padStart(2, '0')
        return "$grouped,$fraction ${currency.symbol}"
    }

    companion object {
        private const val CENTS_PER_UNIT = 100L
        private const val MAX_DECIMALS = 2
        private val INPUT = Regex("^([0-9]{1,12})(?:[.,]([0-9]{1,2}))?$")

        /**
         * Parses what a user types in a price field: `45`, `45,5`, `45,50` or `45.50`.
         * Thousands separators, signs, more than two decimals and blank input return null.
         */
        fun parse(input: String, currency: Currency = Currency.EUR): Money? {
            val match = INPUT.matchEntire(input.trim()) ?: return null
            val units = match.groupValues[1].toLong()
            val fraction = match.groupValues[2].padEnd(MAX_DECIMALS, '0').ifEmpty { "00" }.toLong()
            return Money(units * CENTS_PER_UNIT + fraction, currency)
        }
    }
}
