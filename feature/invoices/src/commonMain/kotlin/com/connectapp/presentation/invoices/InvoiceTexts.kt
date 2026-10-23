package com.connectapp.presentation.invoices

import com.connectapp.commonresources.document_type_nif_nie
import com.connectapp.commonresources.document_type_other
import com.connectapp.commonresources.document_type_passport
import com.connectapp.commonresources.error_invoice_not_author
import com.connectapp.commonresources.error_invoice_voided
import com.connectapp.commonresources.error_patient_not_available
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.invoice_not_found
import com.connectapp.commonresources.payment_method_bizum
import com.connectapp.commonresources.payment_method_card
import com.connectapp.commonresources.payment_method_cash
import com.connectapp.commonresources.payment_method_other
import com.connectapp.commonresources.payment_method_transfer
import com.connectapp.domain.invoices.InvoiceError
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.shared.validation.IdentityDocumentType
import org.jetbrains.compose.resources.StringResource

internal fun Throwable.toMessage(): StringResource = when (this) {
    InvoiceError.NotFound -> invoice_not_found
    InvoiceError.NotAuthor -> error_invoice_not_author
    InvoiceError.Voided -> error_invoice_voided
    InvoiceError.PatientNotAvailable -> error_patient_not_available
    else -> error_unexpected
}

internal val PaymentMethod.label: StringResource
    get() = when (this) {
        PaymentMethod.CASH -> payment_method_cash
        PaymentMethod.CARD -> payment_method_card
        PaymentMethod.TRANSFER -> payment_method_transfer
        PaymentMethod.BIZUM -> payment_method_bizum
        PaymentMethod.OTHER -> payment_method_other
    }

internal val IdentityDocumentType.label: StringResource
    get() = when (this) {
        IdentityDocumentType.NIF_NIE -> document_type_nif_nie
        IdentityDocumentType.PASSPORT -> document_type_passport
        IdentityDocumentType.OTHER -> document_type_other
    }
