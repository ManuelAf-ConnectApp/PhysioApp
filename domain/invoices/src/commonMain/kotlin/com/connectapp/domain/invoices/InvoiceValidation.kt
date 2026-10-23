package com.connectapp.domain.invoices

import com.connectapp.domain.shared.money.Money

enum class InvoiceField { PATIENT, CONCEPT, AMOUNT, VOID_REASON }

/** A patient, a concept and an exact amount greater than zero are required. */
object InvoiceValidation {
    const val MAX_CONCEPT_LENGTH = 200

    fun errors(patientId: String?, concept: String, amountText: String): Set<InvoiceField> = buildSet {
        if (patientId.isNullOrBlank()) add(InvoiceField.PATIENT)
        if (concept.isBlank() || concept.trim().length > MAX_CONCEPT_LENGTH) add(InvoiceField.CONCEPT)
        if (parseAmount(amountText) == null) add(InvoiceField.AMOUNT)
    }

    /** The typed amount (`45`, `45,5`, `45,50`, `45.50`) when it is greater than zero. */
    fun parseAmount(text: String): Money? = Money.parse(text)?.takeIf { it.cents > 0 }

    /** Voiding needs a reason (D6, D8). */
    fun isValidVoidReason(reason: String): Boolean = reason.isNotBlank()
}
