package com.connectapp.presentation.patients.detail

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.patients.DeletePatientUseCase
import com.connectapp.domain.patients.ObservePatientSignatureImageUseCase
import com.connectapp.domain.patients.ObservePatientUseCase
import com.connectapp.domain.patients.Patient
import com.connectapp.domain.patients.SetPatientArchivedUseCase
import com.connectapp.presentation.patients.toMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

enum class PatientDialog { ARCHIVE, DELETE }

class PatientDetailState(
    val patient: Patient? = null,
    /** PNG of the latest signature; never logged. */
    val signaturePng: ByteArray? = null,
    val isLoading: Boolean = true,
    val dialog: PatientDialog? = null,
    val errorMessage: StringResource? = null,
) {
    fun copy(
        patient: Patient? = this.patient,
        signaturePng: ByteArray? = this.signaturePng,
        isLoading: Boolean = this.isLoading,
        dialog: PatientDialog? = this.dialog,
        errorMessage: StringResource? = this.errorMessage,
    ) = PatientDetailState(patient, signaturePng, isLoading, dialog, errorMessage)

    override fun toString(): String = "PatientDetailState(isLoading=$isLoading, dialog=$dialog)"
}

sealed interface PatientDetailIntent {
    object ArchiveClicked : PatientDetailIntent
    object UnarchiveClicked : PatientDetailIntent
    object DeleteClicked : PatientDetailIntent
    object DialogConfirmed : PatientDetailIntent
    object DialogDismissed : PatientDetailIntent
}

sealed interface PatientDetailEffect {
    object Deleted : PatientDetailEffect
}

/** Patient record with its latest signed consent and its actions (D8, D13). */
class PatientDetailViewModel(
    private val patientId: String,
    observePatient: ObservePatientUseCase,
    observeSignatureImage: ObservePatientSignatureImageUseCase,
    private val setArchived: SetPatientArchivedUseCase,
    private val deletePatient: DeletePatientUseCase,
) : MviViewModel<PatientDetailState, PatientDetailIntent, PatientDetailEffect>(PatientDetailState()) {

    init {
        observePatient(patientId)
            .onEach { patient -> updateState { it.copy(patient = patient, isLoading = false) } }
            .launchIn(viewModelScope)
        observeSignatureImage(patientId)
            .onEach { png -> updateState { it.copy(signaturePng = png) } }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: PatientDetailIntent) {
        when (intent) {
            PatientDetailIntent.ArchiveClicked -> openDialog(PatientDialog.ARCHIVE)
            PatientDetailIntent.DeleteClicked -> openDialog(PatientDialog.DELETE)
            PatientDetailIntent.UnarchiveClicked -> archive(false)
            PatientDetailIntent.DialogDismissed -> updateState { it.copy(dialog = null) }
            PatientDetailIntent.DialogConfirmed -> when (state.value.dialog) {
                PatientDialog.ARCHIVE -> archive(true)
                PatientDialog.DELETE -> delete()
                null -> Unit
            }
        }
    }

    private fun openDialog(dialog: PatientDialog) = updateState { it.copy(dialog = dialog, errorMessage = null) }

    private fun archive(archived: Boolean) {
        updateState { it.copy(dialog = null, errorMessage = null) }
        viewModelScope.launch {
            setArchived(patientId, archived).onFailure { error ->
                updateState { it.copy(errorMessage = error.toMessage()) }
            }
        }
    }

    private fun delete() {
        updateState { it.copy(dialog = null, errorMessage = null) }
        viewModelScope.launch {
            deletePatient(patientId)
                .onSuccess { emitEffect(PatientDetailEffect.Deleted) }
                .onFailure { error -> updateState { it.copy(errorMessage = error.toMessage()) } }
        }
    }
}
