package com.connectapp.presentation.invoices.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.badge_unpaid
import com.connectapp.commonresources.button_annul
import com.connectapp.commonresources.button_cancel
import com.connectapp.commonresources.button_export_pdf
import com.connectapp.commonresources.button_mark_paid
import com.connectapp.commonresources.button_mark_unpaid
import com.connectapp.commonresources.component.EmptyState
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.component.RecordField
import com.connectapp.commonresources.component.SelectField
import com.connectapp.commonresources.export.DocumentExportDialog
import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.invoice_issued_on
import com.connectapp.commonresources.invoice_not_author_notice
import com.connectapp.commonresources.invoice_not_fiscal_explanation
import com.connectapp.commonresources.invoice_not_fiscal_notice
import com.connectapp.commonresources.invoice_not_found
import com.connectapp.commonresources.invoice_paid_with
import com.connectapp.commonresources.invoice_vat_exempt
import com.connectapp.commonresources.invoice_voided_on
import com.connectapp.commonresources.label_amount_plain
import com.connectapp.commonresources.label_annulment_reason
import com.connectapp.commonresources.label_concept
import com.connectapp.commonresources.label_paid
import com.connectapp.commonresources.label_patient
import com.connectapp.commonresources.label_payment_method
import com.connectapp.commonresources.label_payment_status
import com.connectapp.commonresources.label_professional
import com.connectapp.commonresources.payment_method_none
import com.connectapp.commonresources.void_invoice_message
import com.connectapp.commonresources.void_invoice_title
import com.connectapp.domain.invoices.Invoice
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.presentation.invoices.label
import com.connectapp.presentation.invoices.pdf.exportInvoicePdf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A receipt as the detail pane of the search or as a screen of its own. Exporting is open to
 * everyone; marking as paid or pending and voiding only to its author while it is not voided.
 */
@Composable
fun InvoiceDetail(
    invoiceId: String,
    modifier: Modifier = Modifier,
    exportOnOpen: Boolean = false,
) {
    val viewModel: InvoiceDetailViewModel =
        koinViewModel(key = "invoice-detail-$invoiceId") { parametersOf(invoiceId, exportOnOpen) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val invoice = state.invoice
    val onIntent = viewModel::onIntent

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            invoice == null ->
                EmptyState(Icons.AutoMirrored.Outlined.ReceiptLong, stringResource(invoice_not_found), "")
            else -> DetailContent(invoice, state, onIntent)
        }
    }

    val dismiss = { onIntent(InvoiceDetailIntent.DialogDismissed) }
    when (val dialog = state.dialog) {
        is InvoiceDialog.Void -> VoidDialog(
            reason = dialog.reason,
            canConfirm = state.canConfirmVoid,
            onReasonChange = { onIntent(InvoiceDetailIntent.VoidReasonChanged(it)) },
            onConfirm = { onIntent(InvoiceDetailIntent.VoidConfirmed) },
            onDismiss = dismiss,
        )
        is InvoiceDialog.MarkPaid -> MarkPaidDialog(
            method = dialog.method,
            onMethodChange = { onIntent(InvoiceDetailIntent.PaymentMethodChanged(it)) },
            onConfirm = { onIntent(InvoiceDetailIntent.MarkPaidConfirmed) },
            onDismiss = dismiss,
        )
        InvoiceDialog.Export -> if (invoice != null) {
            DocumentExportDialog(build = { exportInvoicePdf(invoice) }, onDismiss = dismiss)
        }
        null -> Unit
    }
}

/** D6: on screen as in the PDF, the receipt says that it is not an invoice. */
@Composable
internal fun NotFiscalNotice(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(invoice_not_fiscal_notice),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(invoice_not_fiscal_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun DetailContent(invoice: Invoice, state: InvoiceDetailState, onIntent: (InvoiceDetailIntent) -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = invoice.number,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(invoice_issued_on, formatDateTime(invoice.issuedAtEpochMillis)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        invoice.voiding?.let { voiding ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(
                        invoice_voided_on,
                        formatDateTime(voiding.voidedAtEpochMillis),
                        voiding.reason,
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        NotFiscalNotice()

        val recipient = invoice.recipient
        RecordField(label_patient, "${recipient.firstName} ${recipient.lastName} · ${recipient.documentNumber}")
        RecordField(label_professional, "${invoice.issuer.firstName} ${invoice.issuer.lastName}")
        RecordField(label_concept, invoice.concept)
        RecordField(label_amount_plain, invoice.amount.format())
        RecordField(label_payment_status, paymentStatus(invoice))
        Text(
            text = stringResource(invoice_vat_exempt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.errorMessage?.let {
            ErrorMessage(stringResource(it))
        }
        Actions(invoice, state, onIntent)
    }
}

@Composable
private fun paymentStatus(invoice: Invoice): String {
    val method = invoice.paymentMethod
    return when {
        !invoice.paid -> stringResource(badge_unpaid)
        method != null -> stringResource(invoice_paid_with, stringResource(method.label))
        else -> stringResource(label_paid)
    }
}

@Composable
private fun Actions(invoice: Invoice, state: InvoiceDetailState, onIntent: (InvoiceDetailIntent) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = { onIntent(InvoiceDetailIntent.ExportClicked) }) { Text(stringResource(button_export_pdf)) }
        if (state.canChange) {
            if (invoice.paid) {
                OutlinedButton(onClick = { onIntent(InvoiceDetailIntent.MarkUnpaidClicked) }) {
                    Text(stringResource(button_mark_unpaid))
                }
            } else {
                OutlinedButton(onClick = { onIntent(InvoiceDetailIntent.MarkPaidClicked) }) {
                    Text(stringResource(button_mark_paid))
                }
            }
            OutlinedButton(
                onClick = { onIntent(InvoiceDetailIntent.VoidClicked) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(button_annul))
            }
        }
    }
    if (!state.isAuthor && !invoice.isVoided) {
        Text(
            text = stringResource(invoice_not_author_notice),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VoidDialog(
    reason: String,
    canConfirm: Boolean,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(void_invoice_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(void_invoice_message))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(label_annulment_reason)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = canConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(button_annul))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(button_cancel)) } },
    )
}

@Composable
private fun MarkPaidDialog(
    method: PaymentMethod?,
    onMethodChange: (PaymentMethod?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val methods = listOf<PaymentMethod?>(null) + PaymentMethod.entries
    val labels = methods.map { stringResource(it?.label ?: payment_method_none) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(button_mark_paid)) },
        text = {
            SelectField(
                selected = labels[methods.indexOf(method)],
                options = labels,
                onSelected = { index -> onMethodChange(methods[index]) },
                label = label_payment_method,
                error = null,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(button_mark_paid)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(button_cancel)) } },
    )
}

/** The receipt as a screen of its own (windows too narrow for list and detail side by side). */
@Composable
fun InvoiceDetailScreen(invoiceId: String, exportOnOpen: Boolean, modifier: Modifier = Modifier) {
    InvoiceDetail(invoiceId = invoiceId, exportOnOpen = exportOnOpen, modifier = modifier)
}
