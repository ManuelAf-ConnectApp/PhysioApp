package com.connectapp.presentation.invoices.detail

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.invoices.Invoice
import com.connectapp.domain.invoices.InvoiceValidation
import com.connectapp.domain.invoices.ObserveInvoiceUseCase
import com.connectapp.domain.invoices.PaymentMethod
import com.connectapp.domain.invoices.SetInvoicePaidUseCase
import com.connectapp.domain.invoices.VoidInvoiceUseCase
import com.connectapp.domain.usecase.CurrentAuthorUseCase
import com.connectapp.presentation.invoices.toMessage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/** Dialog open on top of the receipt. */
sealed interface InvoiceDialog {
    data class Void(val reason: String = "") : InvoiceDialog
    data class MarkPaid(val method: PaymentMethod? = null) : InvoiceDialog

    /** The PDF warning with its share/save choice (D19). */
    object Export : InvoiceDialog
}

data class InvoiceDetailState(
    val invoice: Invoice? = null,
    /** The signed-in professional issued it: only then its status can change or it can be voided (D5). */
    val isAuthor: Boolean = false,
    val isLoading: Boolean = true,
    val dialog: InvoiceDialog? = null,
    val errorMessage: StringResource? = null,
) {
    val canChange: Boolean get() = isAuthor && invoice?.isVoided == false
    val canConfirmVoid: Boolean
        get() = (dialog as? InvoiceDialog.Void)?.reason?.let(InvoiceValidation::isValidVoidReason) == true

    override fun toString(): String = "InvoiceDetailState(isLoading=$isLoading, isAuthor=$isAuthor)"
}

sealed interface InvoiceDetailIntent {
    object ExportClicked : InvoiceDetailIntent
    object VoidClicked : InvoiceDetailIntent
    data class VoidReasonChanged(val reason: String) : InvoiceDetailIntent
    object VoidConfirmed : InvoiceDetailIntent
    object MarkPaidClicked : InvoiceDetailIntent
    data class PaymentMethodChanged(val method: PaymentMethod?) : InvoiceDetailIntent
    object MarkPaidConfirmed : InvoiceDetailIntent
    object MarkUnpaidClicked : InvoiceDetailIntent
    object DialogDismissed : InvoiceDetailIntent
}

/**
 * A receipt (D6). Anyone may export it; its author may change its collection status or void it
 * with a reason. With [exportOnOpen] ("Guardar y exportar PDF") the export dialog opens at once.
 */
class InvoiceDetailViewModel(
    private val invoiceId: String,
    exportOnOpen: Boolean,
    observeInvoice: ObserveInvoiceUseCase,
    currentAuthor: CurrentAuthorUseCase,
    private val setPaid: SetInvoicePaidUseCase,
    private val voidInvoice: VoidInvoiceUseCase,
) : StateViewModel<InvoiceDetailState, InvoiceDetailIntent>(
    InvoiceDetailState(dialog = InvoiceDialog.Export.takeIf { exportOnOpen }),
) {

    init {
        combine(observeInvoice(invoiceId), currentAuthor.observe()) { invoice, author -> invoice to author }
            .onEach { (invoice, author) ->
                updateState {
                    it.copy(
                        invoice = invoice,
                        isAuthor = invoice != null && invoice.issuer.professionalId == author,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: InvoiceDetailIntent) {
        when (intent) {
            InvoiceDetailIntent.ExportClicked -> openDialog(InvoiceDialog.Export)
            InvoiceDetailIntent.VoidClicked -> openDialog(InvoiceDialog.Void())
            is InvoiceDetailIntent.VoidReasonChanged ->
                updateDialog<InvoiceDialog.Void> { it.copy(reason = intent.reason) }
            InvoiceDetailIntent.VoidConfirmed -> void()
            InvoiceDetailIntent.MarkPaidClicked -> openDialog(InvoiceDialog.MarkPaid())
            is InvoiceDetailIntent.PaymentMethodChanged ->
                updateDialog<InvoiceDialog.MarkPaid> { it.copy(method = intent.method) }
            InvoiceDetailIntent.MarkPaidConfirmed -> {
                val method = (state.value.dialog as? InvoiceDialog.MarkPaid)?.method
                changePaid(paid = true, method = method)
            }
            InvoiceDetailIntent.MarkUnpaidClicked -> changePaid(paid = false, method = null)
            InvoiceDetailIntent.DialogDismissed -> updateState { it.copy(dialog = null) }
        }
    }

    private fun openDialog(dialog: InvoiceDialog) = updateState { it.copy(dialog = dialog, errorMessage = null) }

    private inline fun <reified T : InvoiceDialog> updateDialog(crossinline change: (T) -> T) = updateState { state ->
        (state.dialog as? T)?.let { state.copy(dialog = change(it)) } ?: state
    }

    private fun changePaid(paid: Boolean, method: PaymentMethod?) {
        updateState { it.copy(dialog = null, errorMessage = null) }
        viewModelScope.launch {
            setPaid(invoiceId, paid, method).onFailure { error ->
                updateState { it.copy(errorMessage = error.toMessage()) }
            }
        }
    }

    private fun void() {
        val reason = (state.value.dialog as? InvoiceDialog.Void)?.reason ?: return
        if (!state.value.canConfirmVoid) return
        updateState { it.copy(dialog = null) }
        viewModelScope.launch {
            voidInvoice(invoiceId, reason).onFailure { error ->
                updateState { it.copy(errorMessage = error.toMessage()) }
            }
        }
    }
}
