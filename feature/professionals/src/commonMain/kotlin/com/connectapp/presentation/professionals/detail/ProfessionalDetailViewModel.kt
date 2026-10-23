package com.connectapp.presentation.professionals.detail

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.professionals.DeleteProfessionalUseCase
import com.connectapp.domain.professionals.ObserveProfessionalUseCase
import com.connectapp.domain.professionals.SetProfessionalArchivedUseCase
import com.connectapp.presentation.professionals.toMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Record of one professional with its actions (D8, confirmed 2026-10-20): archiving asks for
 * confirmation, restoring does not (it hides nothing), and deleting for good asks for
 * confirmation and is only offered for records without account, reports or invoices.
 */
class ProfessionalDetailViewModel(
    private val professionalId: String,
    observeProfessional: ObserveProfessionalUseCase,
    private val setArchived: SetProfessionalArchivedUseCase,
    private val deleteProfessional: DeleteProfessionalUseCase,
) : MviViewModel<ProfessionalDetailState, ProfessionalDetailIntent, ProfessionalDetailEffect>(
    ProfessionalDetailState(),
) {

    init {
        observeProfessional(professionalId)
            .onEach { professional -> updateState { it.copy(professional = professional, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: ProfessionalDetailIntent) {
        when (intent) {
            ProfessionalDetailIntent.ArchiveClicked -> openDialog(ProfessionalDialog.ARCHIVE)
            ProfessionalDetailIntent.DeleteClicked -> openDialog(ProfessionalDialog.DELETE)
            ProfessionalDetailIntent.UnarchiveClicked -> archive(false)
            ProfessionalDetailIntent.DialogDismissed -> updateState { it.copy(dialog = null) }
            ProfessionalDetailIntent.DialogConfirmed -> when (state.value.dialog) {
                ProfessionalDialog.ARCHIVE -> archive(true)
                ProfessionalDialog.DELETE -> delete()
                null -> Unit
            }
        }
    }

    private fun openDialog(dialog: ProfessionalDialog) = updateState { it.copy(dialog = dialog, errorMessage = null) }

    private fun archive(archived: Boolean) {
        updateState { it.copy(dialog = null, errorMessage = null) }
        viewModelScope.launch {
            setArchived(professionalId, archived).onFailure { error ->
                updateState { it.copy(errorMessage = error.toMessage()) }
            }
        }
    }

    private fun delete() {
        updateState { it.copy(dialog = null, errorMessage = null) }
        viewModelScope.launch {
            deleteProfessional(professionalId)
                .onSuccess { emitEffect(ProfessionalDetailEffect.Deleted) }
                .onFailure { error -> updateState { it.copy(errorMessage = error.toMessage()) } }
        }
    }
}
